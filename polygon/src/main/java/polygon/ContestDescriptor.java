package polygon;

import org.xml.sax.SAXException;
import xmlwrapper.XMLElement;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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

        XMLElement contestElement = XMLElement.getRoot(contest.xmlFile);

        contest.url = contestElement.getAttribute("url");

        //names
        contest.contestNames = new TreeMap<>();
        for (XMLElement nameElement : contestElement.findFirstChild("names").findChildren("name")) {
            contest.contestNames.put(nameElement.getAttribute("language"), nameElement.getAttribute("value"));
        }

        //statements
        contest.statementLinks = new TreeMap<>();
        for (XMLElement statementElement : contestElement.findFirstChild("statements").findChildren("statement")) {
            contest.statementLinks.put(statementElement.getAttribute("language"), statementElement.getAttribute("url"));
        }

        //problems
        contest.problemLinks = new TreeMap<>();
        for (XMLElement problemElement : contestElement.findFirstChild("problems").findChildren("problem")) {
            contest.problemLinks.put(problemElement.getAttribute("index"), problemElement.getAttribute("url"));
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
