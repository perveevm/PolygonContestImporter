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
import java.util.TreeMap;

public class Contest {
    File xmlFile;

    String url;
    //language -> name
    TreeMap<String, String> names;
    //problem index maps to problem
    TreeMap<String, Problem> problems;

    public static Contest parse(String path) throws ParserConfigurationException, IOException, SAXException {
        System.out.println("parsing contest.xml ...");

        Contest contest = new Contest();
        contest.problems = new TreeMap<>();
        contest.names = new TreeMap<>();
        contest.xmlFile = new File(path, "contest.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(contest.xmlFile);

        Element contestElement = doc.getDocumentElement();
        contest.url = contestElement.getAttribute("url");

        //names
        Element namesElement = (Element) contestElement.getElementsByTagName("names").item(0);
        NodeList nameNodes = namesElement.getElementsByTagName("name");
        for (int i = 0; i < nameNodes.getLength(); i++) {
            Element nameElement = (Element) nameNodes.item(i);
            contest.names.put(nameElement.getAttribute("language"), nameElement.getAttribute("value"));
        }

        //problems
        Element problemsElement = (Element) contestElement.getElementsByTagName("problems").item(0);
        NodeList problemNodes = problemsElement.getElementsByTagName("problem");
        for (int i = 0; i < problemNodes.getLength(); i++) {
            Element child = (Element) problemNodes.item(i);
            String purl = child.getAttribute("url");
            String index = child.getAttribute("index");
            String pname = purl.substring(purl.lastIndexOf("/") + 1);
            Problem problem = Problem.parse(new File(path, "problems/" + pname).getAbsolutePath());
            if (!problem.url.equals(purl)) {
                System.out.println("ERROR: Problem URL do not match! Contest problem = '" + purl + "' problems.xml = '" + problem.url + "'");
                System.exit(1);
            }
            contest.problems.put(index, problem);
        }
        return contest;
    }

    public String getUrl() {
        return url;
    }

    public TreeMap<String, String> getNames() {
        return names;
    }

    public TreeMap<String, Problem> getProblems() {
        return problems;
    }
}
