import org.apache.commons.lang3.time.StopWatch;

import java.io.Console;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrabManager {
    private int threadCount;
    private static final long PAUSE_TIME = 500;

    private String host;
    private Console console;

    private Connection connection;

    private Set<URL> masterList = new HashSet<>();
    private List<Future<GrabPage>> futures = new ArrayList<>();
    private ExecutorService executorService;


    private final int maxDepth;

    public GrabManager(int maxDepth, int concurrentThreads, String host, Connection connection, Console console) {
        this.maxDepth = maxDepth;
        this.host = host;
        this.connection = connection;
        this.console = console;
        this.threadCount = concurrentThreads;
        executorService = Executors.newFixedThreadPool(threadCount);

        createTable(host);

    }

    public void createTable(String tname) {
        try {
            CallableStatement c = connection.prepareCall("{call create_table(?)}");
            c.setString(1, tname);
            c.execute();
        } catch (Exception e) {
            console.printf("Error in creating table " + e.getMessage());
        }
    }


    public void go(URL start) throws IOException, InterruptedException, SQLException {

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        submitNewURL(start, 0);

        while (checkPageGrabs()) ;

        stopWatch.stop();

        console.printf(stopWatch.getTime() / 1000 + " seconds\n");
        executorService.shutdown();
    }

    private boolean checkPageGrabs() throws InterruptedException, MalformedURLException, SQLException {

        Thread.sleep(PAUSE_TIME);
        Set<GrabPage> pageSet = new HashSet<>();
        Iterator<Future<GrabPage>> iterator = futures.iterator();

        while (iterator.hasNext()) {
            Future<GrabPage> future = iterator.next();

            if (future.isDone()) {
                iterator.remove();
                try {
                    pageSet.add(future.get());
                } catch (Exception e) {
                }
            }
        }

        for (GrabPage grabPage : pageSet) {
            addNewURLs(grabPage);
        }

        return (futures.size() > 0);
    }


    private void addNewURLs(GrabPage grabPage) throws MalformedURLException, SQLException {
        for (URL url : grabPage.getUrlList()) {
            submitNewURL(url, grabPage.getDepth() + 1);
        }
    }


    private void submitNewURL(URL url, int depth) throws MalformedURLException, SQLException {
        if (shouldVisit(url, depth)) {
            masterList.add(url);

            GrabPage grabPage = new GrabPage(url, depth, host, connection, console);
            Future<GrabPage> future = executorService.submit(grabPage);
            futures.add(future);
        }
    }

    public boolean checkDomainName(URL url) throws MalformedURLException {
        String test = url.getProtocol() + "://" + url.getHost();

        Pattern p = Pattern.compile("https?://(www.)*([a-zA-Z\\d-]+.)*(" + host + ")");

        Matcher m = p.matcher(test);
        return m.matches();
    }

    private boolean checkEnding(URL url) {
        String urll = url.toString().toLowerCase();//edited so get all combs8
        return !urll.endsWith(".pdf") &&
                !urll.endsWith(".doc") &&
                !urll.endsWith(".docx") &&
                !urll.endsWith(".jpg") &&
                !urll.endsWith(".jpeg") &&
                !urll.endsWith(".png") &&
                !urll.endsWith("#");
    }

    private boolean checkContains(URL url) {
        String urll = url.toString().toLowerCase();
        return !urll.contains("news") &&
                !urll.contains("journal") &&
                !urll.contains("calender") &&
                !urll.contains("librar") &&
                !urll.contains("authentication") &&
                !urll.contains("form") &&
                !urll.contains("alumni") &&
                !urll.contains("arabic") &&
                !urll.contains("elearning") &&
                !urll.contains("visitors");
    }

    private boolean shouldVisit(URL url, int depth) throws MalformedURLException {
        if (masterList.contains(url)) {
            return false;
        }
        if (!checkDomainName(url)) {
            return false;
        }
        if (!checkEnding(url)) {
            return false;
        }
        if (depth > maxDepth) {
            return false;
        }
        if (!checkContains(url))
            return false;
        return true;
    }


}