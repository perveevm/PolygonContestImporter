package polygon;

import org.xml.sax.SAXException;
import xmlwrapper.XMLElement;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class ProblemDescriptor {
    protected File xmlFile;
    protected String shortName;
    protected int revision;
    protected String url;
    protected String input;
    protected String output;
    protected Checker checker;
    protected Interactor interactor;

    //key - language, value - name
    protected TreeMap<String, String> names;
    //key - testset name, value - testset
    protected TreeMap<String, Testset> testsets;
    protected ArrayList<Attachment> attachments;
    protected ArrayList<Solution> solutions;
    protected List<SolutionResource> solutionResources;

    protected ProblemDescriptor(File xmlFile) throws FileNotFoundException {
        if (!xmlFile.exists()) {
            throw new FileNotFoundException("ERROR: Couldn't find directory or file '" + xmlFile + "'");
        }
        this.xmlFile = xmlFile;
        testsets = new TreeMap<>();
        names = new TreeMap<>();
        attachments = new ArrayList<>();
        solutions = new ArrayList<>();
        solutionResources = new ArrayList<>();
    }

    public static ProblemDescriptor parse(File xmlFile) throws IOException, ParserConfigurationException, SAXException {
        ProblemDescriptor p = new ProblemDescriptor(xmlFile);
        p.parseDescriptor();
        return p;
    }

    protected void parseDescriptor() throws IOException, SAXException, ParserConfigurationException {
        XMLElement problemElement = XMLElement.getRoot(xmlFile);

        shortName = problemElement.getAttribute("short-name");
        revision = Integer.parseInt(problemElement.getAttribute("revision"));
        System.out.println("parsing problem '" + shortName + "'");

        url = problemElement.getAttribute("url");

        //names
        for (XMLElement nameElement : problemElement.findFirstChild("names").findChildren("name")) {
            names.put(nameElement.getAttribute("language"), nameElement.getAttribute("value"));
        }

        //judging
        XMLElement judgingElement = problemElement.findFirstChild("judging");
        input = judgingElement.getAttribute("input-file");
        output = judgingElement.getAttribute("output-file");

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
