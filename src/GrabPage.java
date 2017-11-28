import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.Console;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrabPage implements Callable<GrabPage> {
    static final int TIMEOUT = 60000;   // one minute
    private static final Pattern hrefPattern = Pattern.compile("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private Connection connection;
    private Console console;
    private String host;

    private URL url;
    private int depth;
    private String data;
    private int hashCode;
    private String domain;
    private String title;
    private Set<URL> urlList = new HashSet<>();


    public GrabPage(URL url, int depth, String host, Connection c, Console console) {
        this.url = url;
        this.depth = depth;
        this.connection = c;
        this.console = console;
        this.host = host;
    }

    @Override
    public GrabPage call() throws Exception {

        try {
            console.printf("Visiting url : %s , depth : (%d)\n", url.toString(), depth);
            Document document;
            document = Jsoup.parse(url, TIMEOUT);


            document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
            this.data = document.body().text();

            this.hashCode = url.toString().hashCode();
            this.domain = url.getHost();
            this.title = document.title();


            processLinks(document.select("a[href]"));

            sqlExec(url.toString(), hashCode, domain, title, data);
        } catch (Exception e) {
        }
        return this;
    }

    public void sqlExec(String href, int hashCode, String domain, String title, String data)
            throws SQLException {
        try {

            Clob n = connection.createClob();
            n.setString(1, data);

            PreparedStatement p = connection.prepareStatement("insert into `" + host + "` values (?,?,?,?,?)");

            p.setInt(1, hashCode);
            p.setString(2, href);
            p.setString(3, domain);
            p.setString(4, title);
            p.setClob(5, n);

            p.execute();
        } catch (Exception e) {

            console.printf(e.getMessage() + "\n");
        }
    }

    private boolean isLink(String str) {
        Matcher matcher = hrefPattern.matcher(str);
        return matcher.matches();
    }

    private void processLinks(Elements links) {

        for (Element link : links) {
            String href = link.attr("abs:href");
            href = href.replaceAll(" ", "%20");
            if (!isLink(href)) {
                continue;
            }
            try {
                URL nextUrl = new URL(href);
                urlList.add(nextUrl);
            } catch (MalformedURLException e) { // ignore bad urls
            }
        }
    }

    public Set<URL> getUrlList() {
        return urlList;
    }

    public int getDepth() {
        return depth;
    }

    public int getHashCode() {
        return hashCode;
    }

    public String getDomain() {
        return domain;
    }

    public String getTitle() {
        return title;
    }

    public URL getUrl() {
        return url;
    }

    public String getData() {
        return data;
    }
}