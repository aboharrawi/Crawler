import jdk.internal.org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CrawlerStartup {

    public static void main(String[] args) {

        //read file
        //parse xml

        Console c = System.console();
        try {
            //java -jar Engine.jar url userName password domain base depth
            if (args.length != 4)
                throw new IllegalArgumentException("usage <java -jar Crawler.jar url userName password inputFile");

            String url = args[0];
            String user = args[1];
            String pass = args[2];

            String fileName = args[3];
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + url, user, pass);

            ArrayList<XMLFormat> db = parse(fileName);

            GrabManager grabManager;
            for (XMLFormat x : db) {
                grabManager = new GrabManager(x.getDepth(), x.getThreads(), x.getDomain(), connection, c);
                grabManager.go(new URL(x.getBase()));


            }

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
        }


    }

    private static ArrayList<XMLFormat> parse(String fileName)
            throws IOException, ParserConfigurationException, SAXException, ParseException, org.xml.sax.SAXException {


        ArrayList<XMLFormat> arrayList = new ArrayList<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(fileName));

        doc.getDocumentElement().normalize();
        doc.getDocumentElement().getNodeName();

        NodeList nodeList = doc.getElementsByTagName("university");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nd = nodeList.item(i);
            XMLFormat obj = new XMLFormat();
            if (Node.ELEMENT_NODE == nd.getNodeType()) {
                Element element = (Element) nodeList.item(i);
                obj.setDomain(element.getElementsByTagName("domain").item(0).getTextContent());
                obj.setBase(element.getElementsByTagName("base").item(0).getTextContent());
                obj.setDepth(Integer.parseInt(element.getElementsByTagName("depth").item(0).getTextContent()));
                obj.setThreads(Integer.parseInt(element.getElementsByTagName("threads").item(0).getTextContent()));
                arrayList.add(obj);
            }
        }
        return arrayList;
    }
}
