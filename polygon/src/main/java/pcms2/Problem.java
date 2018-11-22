package pcms2;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Problem {
    String xmlPath;
    String groupsPath;
    String id;
    String scriptType;
    String name;
    //key - language, value - name
    TreeMap <String, String> names;
    String shortName;
    String url;
    File problemDirectory;
    String input;
    String output;
    TreeMap <String, Testset> testsets;
    ArrayList<Attachment> attachments;
    Solution[] solutions;
    Verifier verifier;
    Interactor interactor;

    boolean hasPreliminary = false;
    int sampleCount = 0;
    BufferedReader groupstxt;

    public Problem(String path, String idprefix, String type, Properties languageProps, Properties executableProps, String defaultLang) throws Exception {
        problemDirectory = new File(path);
        if (!problemDirectory.exists()) {
            throw new AssertionError("Couldn't find directory");
        }
        xmlPath = path + "/problem.xml";
        groupsPath = path + "/files/groups.txt";
        id = idprefix;
        scriptType = type;
        testsets = new TreeMap<>();
        names = new TreeMap<>();
        parse(languageProps, executableProps, defaultLang);
    }

    public void parse(Properties languageProps, Properties executableProps, String defaultLang) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlPath));
        groupstxt = null;
        if ((new File(groupsPath)).exists()) {
            groupstxt = new BufferedReader(new FileReader(groupsPath));
        }
        //NodeList problem = doc.getDocumentElement().getChildNodes();
        Element el = doc.getDocumentElement();
        shortName = el.getAttribute("short-name");

        System.out.println("\nparsing problem '" + shortName + "'");

        url = el.getAttribute("url");
        if (id.startsWith("com.codeforces.polygon") || id.equals("auto")) {
            String[] t = url.split("/");
            String cflogin = t[t.length - 2];
            if (cflogin.contains(".")) {
                System.out.println("WARNING: Problem owner login contains '.', replacing with '-'");
                cflogin = cflogin.replaceAll("\\.", "-");
            }
            id = "com.codeforces.polygon." + cflogin;
        }
        id = id + "." + shortName;

        //names
        NodeList nodeList = ((Element) doc.getElementsByTagName("names").item(0)).getElementsByTagName("name");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            el = (Element) node;
            names.put(el.getAttribute("language"), el.getAttribute("value"));
        }
        if (names.containsKey(defaultLang)) {
            name = names.get(defaultLang);
        } else {
            name = names.firstEntry().getValue();
            System.out.println("WARNING: Problem name for default language '" + defaultLang + "' not found! Using '" + names.firstKey() + "' name.");
        }

        //judging
        el = (Element) doc.getElementsByTagName("judging").item(0);
        input = el.getAttribute("input-file");
        if (input.isEmpty()) input = "*";
        output = el.getAttribute("output-file");
        if (output.isEmpty()) output = "*";

        //testset
        nodeList = el.getElementsByTagName("testset");
        for (int i = 0; i < nodeList.getLength(); i++) {//testset
            //System.out.println("DEBUG: testsets cnt = " + nl.getLength() + " i = " + i);
            el = (Element) nodeList.item(i);
            Testset ts = Testset.parse(this, el);
            testsets.put(ts.name, ts);
            //System.out.println("testset finished");
        }
        if (!hasPreliminary) {
            System.out.println("INFO: No preliminary testset, getting sample tests");
            Test[] temp = new Test[sampleCount];
            for (int i = 0; i < sampleCount; i++) {
                temp[i] = testsets.get("tests").tests[i];
            }
            Testset preliminary = new Testset();
            preliminary.tests = temp;
            preliminary.name = "preliminary";
            preliminary.inputName = testsets.get("tests").inputName;
            preliminary.outputName = testsets.get("tests").outputName;
            preliminary.inputHref = testsets.get("tests").inputHref;
            preliminary.outputHref = testsets.get("tests").outputHref;
            preliminary.memoryLimit = testsets.get("tests").memoryLimit;
            preliminary.timeLimit = testsets.get("tests").timeLimit;
            testsets.put("preliminary", preliminary);
        }

        //parse groups.txt
        if (testsets.get("tests").groups.size() != 0) {
            Group.parseGroupstxt(groupstxt, testsets.get("tests").groups, testsets.get("tests").groupNameToId);
        }

        //files attachments
        el = (Element)
                ((Element) doc.getElementsByTagName("files").item(0))
                .getElementsByTagName("attachments").item(0);
        attachments = new ArrayList<>();
        if (el != null) {
            nodeList = el.getElementsByTagName("file");
            for (int i = 0; i < nodeList.getLength(); i++) {
                el = (Element) nodeList.item(i);
                Attachment[] attachs = Attachment.parse(this, el, languageProps);
                if (attachs != null) {
                    for (Attachment attach : attachs) {
                        if (attach != null) {
                            attachments.add(attach);
                        }
                    }
                }
            }
        }

        //assets (checker)
        el = (Element) ((Element) doc.getElementsByTagName("assets").item(0)).getElementsByTagName("checker").item(0);
        verifier = Verifier.parse(el, executableProps);
        el = (Element) ((Element) doc.getElementsByTagName("assets").item(0)).
                getElementsByTagName("interactor").item(0);
        interactor = Interactor.parse(el, executableProps);
        if (interactor != null) {
            for (Testset e : testsets.values()) {
                e.inputName = shortName + ".in";
                e.outputName = shortName + ".out";
            }
        }
        el = (Element) ((Element) doc.getElementsByTagName("assets").item(0)).
                getElementsByTagName("solutions").item(0);
        solutions = Solution.parse(el);
    }

    public void print(PrintWriter pw) {
        pw.println("<?xml version = \"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<problem");
        pw.println("\tversion = \"1.0\"");
        pw.println("\tid = \"" + id + "\"");
        pw.println(">");
        pw.println("\t<judging>");

        pw.println("\t\t<script type = \"%" + "icpc" + "\">");

        Testset testset = testsets.get("tests");
        if (testset != null) {
            testset.print(pw, "\t\t\t", "icpc");
        } else {
            System.out.println("WARNING: Testset 'tests' not found! This is the main testset in PCMS.");
        }

        verifier.print(pw, "\t\t\t");
        if (interactor != null) {
            interactor.print(pw, "\t\t\t");
        }
        for (Attachment at : attachments) {
            at.print(pw, "\t\t\t");
        }
        pw.println("\t\t</script>");

        pw.println("\t\t<script type = \"%" + "ioi" + "\">");
        testset = testsets.get("preliminary");
        if (testset != null) {
            testset.print(pw, "\t\t\t", "ioi");
        }
        testset = testsets.get("tests");
        if (testset != null) {
            testset.print(pw, "\t\t\t", "ioi");
        } else {
            System.out.println("WARNING: Testset 'tests' not found! This is the main testset in PCMS.");
        }

        verifier.print(pw, "\t\t\t");
        if (interactor != null) {
            interactor.print(pw, "\t\t\t");
        }
        for (Attachment at : attachments) {
            at.print(pw, "\t\t\t");
        }
        pw.println("\t\t</script>");

        pw.println("\t</judging>");
        pw.println("</problem>");
    }

    void printSolutions(PrintWriter pw, String sessionId, String problemAlias, Properties languageProperties, String vfs) {
        for (Solution sol : solutions) {
            sol.print(pw, sessionId, problemAlias, languageProperties,
                    vfs + "/problems/" + id.replaceAll("\\.", "/"));
        }
    }

    public boolean copyToVFS(String vfs, BufferedReader in, boolean update) throws IOException {
        File src = (new File(xmlPath)).getParentFile();
        File dest = new File(vfs + "/problems/" + id.replaceAll("\\.", "/"));
        //System.out.println("DEBUG: src = '" + src.getAbsolutePath() + " dest = '" + dest.getAbsolutePath() + "'");
        if (dest.exists()) {
            System.out.println("Problem '" + dest.getAbsolutePath() + "' exists.");
            String yn = "n";
            if (!update) {
                System.out.println("Do You want to update it?\n(y - yes, yy - yes to all, n - no)");
                yn = in.readLine();
                if (yn.equals("yy")) {
                    update = true;
                }
            }
            if (update || yn.equals("y")) {
                System.out.println("Updating...");
                FileUtils.copyDirectory(src, dest);
            } else {
                System.out.println("Skipping...");
            }
        } else {
            System.out.println("Copying problem '" + dest.getAbsolutePath() + "'.");
            FileUtils.copyDirectory(src, dest);
        }
        return update;
    }
}
