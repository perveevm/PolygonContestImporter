package polygon;

import org.xml.sax.SAXException;
import xmlwrapper.XMLElement;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class ProblemDescriptor {

    File directory;
    File xmlFile;
    File groupsTxtFile;

    String shortName;
    int revision;
    String url;
    String input;
    String output;
    Checker checker;
    Interactor interactor;

    //key - language, value - name
    TreeMap<String, String> names;
    //key - testset name, value - testset
    TreeMap<String, Testset> testsets;
    ArrayList<Attachment> attachments;
    ArrayList<Solution> solutions;

    private ProblemDescriptor(String path) throws FileNotFoundException {
        File pathFile = new File(path);
        if (!pathFile.exists()) {
            throw new FileNotFoundException("ERROR: Couldn't find directory or file '" + path + "'");
        }
        if (pathFile.isDirectory()) {
            directory = pathFile;
            xmlFile = new File(directory, "problem.xml.polygon");
            if (!xmlFile.exists()) {
                if (!(new File(directory, "problem.xml")).renameTo(xmlFile)) {
                    throw new FileNotFoundException("ERROR: problem.xml not found in '" + directory + "'!");
                }
            }
        } else {
            directory = pathFile.getParentFile();
            xmlFile = pathFile;
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

    public static ProblemDescriptor parse(String path) throws ParserConfigurationException, IOException, SAXException {
        ProblemDescriptor problem = new ProblemDescriptor(path);
        XMLElement problemElement = XMLElement.getRoot(problem.xmlFile);

        problem.shortName = problemElement.getAttribute("short-name");
        problem.revision = Integer.parseInt(problemElement.getAttribute("revision"));
        System.out.println("parsing problem '" + problem.shortName + "'");

        problem.url = problemElement.getAttribute("url");

        //names
        for (XMLElement nameElement : problemElement.findFirstChild("names").findChildren("name")) {
            problem.names.put(nameElement.getAttribute("language"), nameElement.getAttribute("value"));
        }

        //judging
        XMLElement judgingElement = problemElement.findFirstChild("judging");
        problem.input = judgingElement.getAttribute("input-file");
        problem.output = judgingElement.getAttribute("output-file");

        //testset
        for (XMLElement testsetElement : judgingElement.findChildren("testset")) {
            Testset ts = Testset.parse(testsetElement);
            problem.testsets.put(ts.name, ts);
        }

        //files attachments
        XMLElement attachmentsElement = problemElement.findFirstChild("files").findFirstChild("attachments");
        if (attachmentsElement.exists()) {
            for (XMLElement fileElement : attachmentsElement.findChildren("file")) {
                Attachment attach = Attachment.parse(fileElement);
                problem.attachments.add(attach);
            }
        }

        //assets (checker)
        XMLElement assetsElement = problemElement.findFirstChild("assets");
        problem.checker = Checker.parse(assetsElement.findFirstChild("checker"));
        //assets (interactor)
        problem.interactor = Interactor.parse(assetsElement.findFirstChild("interactor"));
        //assets (solutions)
        problem.solutions.addAll(Arrays.asList(Solution.parse(assetsElement.findFirstChild("solutions"))));

        problem.parseGroupsTxt();

        return problem;
    }

    public void parseGroupsTxt() throws IOException {
        if (groupsTxtFile == null) return;
        if (!testsets.containsKey("tests")) return;

        BufferedReader br = new BufferedReader(new FileReader(groupsTxtFile));
        Testset testset = testsets.get("tests");
        if (testset.groups == null) {
            testset.groups = new TreeMap<>();
        }
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
                group.name = group_par.get("group");
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
}
