package polygon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import xmlwrapper.XMLElement;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class ProblemDescriptor {
    private final static Logger log = LogManager.getLogger(ProblemDescriptor.class);
    protected String shortName;
    protected int revision;
    protected String url;
    protected String input;
    protected String output;
    protected String runCount;
    protected Checker checker;
    protected Interactor interactor;

    //key - language, value - name
    protected TreeMap<String, String> names;
    //key - testset name, value - testset
    protected TreeMap<String, Testset> testsets;
    protected ArrayList<Attachment> attachments;
    protected ArrayList<Solution> solutions;
    protected List<SolutionResource> solutionResources;

    protected ProblemDescriptor() {
        testsets = new TreeMap<>();
        names = new TreeMap<>();
        attachments = new ArrayList<>();
        solutions = new ArrayList<>();
        solutionResources = new ArrayList<>();
    }

    public static ProblemDescriptor parse(File xmlFile) throws IOException, ParserConfigurationException, SAXException {
        ProblemDescriptor p = new ProblemDescriptor();
        p.parseDescriptor(xmlFile);
        return p;
    }

    public static ProblemDescriptor parse(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
        ProblemDescriptor p = new ProblemDescriptor();
        p.parseDescriptor(stream);
        return p;
    }

    protected void parseDescriptor(File xmlFile) throws IOException, SAXException, ParserConfigurationException {
        if (!xmlFile.exists()) {
            throw new FileNotFoundException("ERROR: Couldn't find directory or file '" + xmlFile + "'");
        }
        try (FileInputStream stream = new FileInputStream(xmlFile)) {
            parseDescriptor(stream);
        }
    }

    protected void parseDescriptor(InputStream stream) throws IOException, SAXException, ParserConfigurationException {
        XMLElement problemElement = XMLElement.getRoot(stream);

        shortName = problemElement.getAttribute("short-name");
        revision = Integer.parseInt(problemElement.getAttribute("revision"));
        log.info("parsing problem '" + shortName + "'");

        url = problemElement.getAttribute("url");

        //names
        for (XMLElement nameElement : problemElement.findFirstChild("names").findChildren("name")) {
            names.put(nameElement.getAttribute("language"), nameElement.getAttribute("value"));
        }

        //judging
        XMLElement judgingElement = problemElement.findFirstChild("judging");
        input = judgingElement.getAttribute("input-file");
        output = judgingElement.getAttribute("output-file");
        runCount = judgingElement.getAttribute("run-count");


        //testset
        for (XMLElement testsetElement : judgingElement.findChildren("testset")) {
            Testset ts = Testset.parse(testsetElement);
            testsets.put(ts.name, ts);
        }

        //files attachments
        XMLElement attachmentsElement = problemElement.findFirstChild("files").findFirstChild("attachments");
        if (attachmentsElement.exists()) {
            for (XMLElement fileElement : attachmentsElement.findChildren("file")) {
                Attachment attach = Attachment.parse(fileElement);
                attachments.add(attach);
            }
        }

        // resources, solution assets
        XMLElement resourcesElement = problemElement.findFirstChild("files").findFirstChild("resources");
        for (XMLElement fileElement : resourcesElement.findChildren("file")) {
            XMLElement assets = fileElement.findFirstChild("assets");
            if (assets.exists() && assets.findChildrenStream("asset")
                    .anyMatch(x -> "solution".equals(x.getAttribute("name")))) {
                SolutionResource resource = new SolutionResource(
                        fileElement.getAttribute("path"),
                        fileElement.getAttribute("for-types"),
                        fileElement.getAttribute("type"));
                solutionResources.add(resource);
            }
        }

        //assets (checker)
        XMLElement assetsElement = problemElement.findFirstChild("assets");
        checker = Checker.parse(assetsElement.findFirstChild("checker"));
        //assets (interactor)
        interactor = Interactor.parse(assetsElement.findFirstChild("interactor"));
        //assets (solutions)
        solutions.addAll(Arrays.asList(Solution.parse(assetsElement.findFirstChild("solutions"))));

    }

    public String getShortName() {
        return shortName;
    }

    public int getRevision() {
        return revision;
    }

    public String getUrl() {
        return url;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public String getRunCount() {
        return runCount;
    }

    public TreeMap<String, Testset> getTestsets() {
        return testsets;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public ArrayList<Solution> getSolutions() {
        return solutions;
    }

    public TreeMap<String, String> getNames() {
        return names;
    }

    public Checker getChecker() {
        return checker;
    }

    public Interactor getInteractor() {
        return interactor;
    }

    public List<SolutionResource> getSolutionResources() {
        return solutionResources;
    }

}
