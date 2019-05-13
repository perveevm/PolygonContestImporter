package polygon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class Problem {

    File directory;
    File xmlFile;
    File groupsTxtFile;

    String shortName;
    String url;
    String input;
    String output;
    Checker checker;
    Interactor interactor;

    //key - language, value - name
    TreeMap<String, String> names;
    //key - testset name, value - testset
    TreeMap <String, Testset> testsets;
    ArrayList<Attachment> attachments;
    ArrayList<Solution> solutions;

    private Problem(String path) {
        directory = new File(path);
        if (!directory.exists()) {
            throw new AssertionError("ERROR: Couldn't find directory '" + path + "'");
        }
        xmlFile = new File(directory, "problem.xml.polygon");
        if (!xmlFile.exists()) {
            if (!(new File(directory, "problem.xml")).renameTo(xmlFile)) {
                System.out.println("ERROR: problem.xml not found in '" + directory + "'!");
                return;
            }
        }

        groupsTxtFile = new File(path + "/files/groups.txt");
        if (!groupsTxtFile.exists()) {
            groupsTxtFile = null;
        }
        testsets = new TreeMap<>();
        names = new TreeMap<>();
        attachments = new ArrayList<>();
        solutions = new ArrayList<>();
    }

    public static Problem parse(String path) throws ParserConfigurationException, IOException, SAXException {
        Problem problem = new Problem(path);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(problem.xmlFile);

        Element problemElement = doc.getDocumentElement();
        problem.shortName = problemElement.getAttribute("short-name");
        System.out.println("parsing problem '" + problem.shortName + "'");

        problem.url = problemElement.getAttribute("url");

        //names
        NodeList nameNodes = ((Element) doc.getElementsByTagName("names").item(0)).getElementsByTagName("name");
        for (int i = 0; i < nameNodes.getLength(); i++) {
            Element nameElement = (Element) nameNodes.item(i);
            problem.names.put(nameElement.getAttribute("language"), nameElement.getAttribute("value"));
        }

        //judging
        Element judgingElement = (Element) doc.getElementsByTagName("judging").item(0);
        problem.input = judgingElement.getAttribute("input-file");
        problem.output = judgingElement.getAttribute("output-file");

        //testset
        NodeList testsetNodes = judgingElement.getElementsByTagName("testset");
        for (int i = 0; i < testsetNodes.getLength(); i++) {
            Element testsetElement = (Element) testsetNodes.item(i);
            Testset ts = Testset.parse(testsetElement);
            problem.testsets.put(ts.name, ts);
        }

        //files attachments
        Element attachmentsElement = (Element)
                ((Element) doc.getElementsByTagName("files").item(0))
                        .getElementsByTagName("attachments").item(0);
        if (attachmentsElement != null) {
            NodeList attachmentNodes = attachmentsElement.getElementsByTagName("file");
            for (int i = 0; i < attachmentNodes.getLength(); i++) {
                Element fileElement = (Element) attachmentNodes.item(i);
                Attachment attach = Attachment.parse(fileElement);
                problem.attachments.add(attach);
            }
        }

        //assets (checker)
        Element checkerElement = (Element) ((Element) doc.getElementsByTagName("assets").item(0)).
                getElementsByTagName("checker").item(0);
        problem.checker = Checker.parse(checkerElement);
        //assets (interactor)
        Element interactorElement = (Element) ((Element) doc.getElementsByTagName("assets").item(0)).
                getElementsByTagName("interactor").item(0);
        problem.interactor = Interactor.parse(interactorElement);
        //assets (solutions)
        Element solutionsElement = (Element) ((Element) doc.getElementsByTagName("assets").item(0)).
                getElementsByTagName("solutions").item(0);
        problem.solutions.addAll(Arrays.asList(Solution.parse(solutionsElement)));

        problem.parseGroupsTxt();

        return problem;
    }

    public void parseGroupsTxt() throws IOException {
        if (groupsTxtFile == null) return;
        if (!testsets.containsKey("tests")) return;

        BufferedReader br = new BufferedReader(new FileReader(groupsTxtFile));
        Testset testset = testsets.get("tests");
        boolean hasGroups = testset.groups.size() > 0;
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] group_params = line.split("(\t;)|(\t)|(;)");
            TreeMap<String, String> group_par = new TreeMap<>();
            for (int ig = 0; ig < group_params.length; ig++) {
                String[] kv = getKeyAndValue(group_params[ig]);
                group_par.put(kv[0], kv[1]);
            }

            if (!group_par.containsKey("group")) {
                System.out.println("WARNING: Group id was not found! " +
                        "Group parameters:'" + Arrays.toString(group_params) + "'. ");
                continue;
            }

            Group group = testset.groups.get(group_par.get("group"));
            if (group == null) {
                if (hasGroups) {
                    System.out.printf("WARNING: It seems that there are no tests in group '%s', skipping\n", group_par.get("group"));
                    continue;
                }
                group = new Group();
                testset.groups.put(group_par.get("group"), group);
            }
            group.parameters = group_par;
        }
    }

    static String[] getKeyAndValue(String s) {
        //key="value"
        int j = s.indexOf('=');
        String[] ss = new String[2];
        ss[0] = s.substring(0, j).trim();
        ss[1] = s.substring(j + 1).trim();
        ss[1] = ss[1].substring(1, ss[1].length() - 1);
        ss[1] = ss[1].replaceAll("<", "&lt;");
        ss[1] = ss[1].replaceAll(">", "&gt;");

        return ss;
    }

    public File getDirectory() {
        return directory;
    }

    public String getShortName() {
        return shortName;
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
}
