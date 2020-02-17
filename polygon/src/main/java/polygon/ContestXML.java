package polygon;

import org.xml.sax.SAXException;
import xmlwrapper.XMLElement;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ContestXML {
    protected File xmlFile;

    protected String url;
    //language -> name
    protected NavigableMap<String, String> contestNames;
    //problem index maps to polygon links
    protected NavigableMap<String, String> problemLinks;
    // language -> statement link
    protected NavigableMap<String, String> statementLinks;

    protected ContestXML(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        this.xmlFile = xmlFile;
    }

    public static ContestXML parse(File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        System.out.println("parsing contest.xml ...");
        ContestXML contestXML = new ContestXML(xmlFile);
        contestXML.parseXML();
        return contestXML;
    }

    protected void parseXML() throws IOException, SAXException, ParserConfigurationException {
        XMLElement contestElement = XMLElement.getRoot(xmlFile);

        url = contestElement.getAttribute("url");

        //names
        contestNames = new TreeMap<>();
        for (XMLElement nameElement : contestElement.findFirstChild("names").findChildren("name")) {
            contestNames.put(nameElement.getAttribute("language"), nameElement.getAttribute("value"));
        }

        //statements
        statementLinks = new TreeMap<>();
        for (XMLElement statementElement : contestElement.findFirstChild("statements").findChildren("statement")) {
            statementLinks.put(statementElement.getAttribute("language"), statementElement.getAttribute("url"));
        }

        //problems
        problemLinks = new TreeMap<>();
        for (XMLElement problemElement : contestElement.findFirstChild("problems").findChildren("problem")) {
            problemLinks.put(problemElement.getAttribute("index"), problemElement.getAttribute("url"));
        }
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
