package polygon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ContestDescriptor {
    File xmlFile;

    String url;
    //language -> name
    NavigableMap<String, String> contestNames;
    //problem index maps to polygon links
    NavigableMap<String, String> problemLinks;
    // language -> statement link
    NavigableMap<String, String> statementLinks;

    public static ContestDescriptor parse(File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        System.out.println("parsing contest.xml ...");

        ContestDescriptor contest = new ContestDescriptor();
        contest.xmlFile = xmlFile;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(contest.xmlFile);

        Element contestElement = doc.getDocumentElement();
        contest.url = contestElement.getAttribute("url");

        //names
        Element namesElement = (Element) contestElement.getElementsByTagName("names").item(0);
        NodeList nameNodes = namesElement.getElementsByTagName("name");
        contest.contestNames = new TreeMap<>();
        for (int i = 0; i < nameNodes.getLength(); i++) {
            Element nameElement = (Element) nameNodes.item(i);
            contest.contestNames.put(nameElement.getAttribute("language"), nameElement.getAttribute("value"));
        }

        //statements
        Element statementsElement = (Element) contestElement.getElementsByTagName("statements").item(0);
        NodeList statementNodes = statementsElement.getElementsByTagName("statement");
        contest.statementLinks = new TreeMap<>();
        for (int i = 0; i < statementNodes.getLength(); i++) {
            Element statementElement = (Element) statementNodes.item(i);
            contest.statementLinks.put(statementElement.getAttribute("language"), statementElement.getAttribute("url"));
        }

        //problems
        Element problemsElement = (Element) contestElement.getElementsByTagName("problems").item(0);
        NodeList problemNodes = problemsElement.getElementsByTagName("problem");
        contest.problemLinks = new TreeMap<>();
        for (int i = 0; i < problemNodes.getLength(); i++) {
            Element child = (Element) problemNodes.item(i);
            contest.problemLinks.put(child.getAttribute("index"), child.getAttribute("url"));
        }
        return contest;
    }

    public String getUrl() {
        return url;
    }

    public NavigableMap<String, String> getContestNames() {
        return contestNames;
    }

    public NavigableMap<String, String> getProblemLinks() {
        return problemLinks;
    }

    public NavigableMap<String, String> getStatementLinks() {
        return statementLinks;
    }
}
