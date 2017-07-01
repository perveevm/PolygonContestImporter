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
    String XMLpath;
    String GroupsPath;
    String ID;
    String ScriptType;
    String Name;
    String shortName;
    String url;
    File problemDirectory;
    String input;
    String output;
    TreeMap <String, Testset> testsets;
    ArrayList<Attachment> attachments;
    Verifier verifier;
    Interactor interactor;

    boolean hasPreliminary = false;
    int sampleCount = 0;
    BufferedReader groupstxt;

    public Problem(String path, String idprefix, String type, Properties languageProps, Properties executableProps) throws Exception {
        problemDirectory = new File(path);
        if (!problemDirectory.exists()) {
            throw new AssertionError("Couldn't find directory");
        }
        XMLpath = path + "/problem.xml";
        GroupsPath = path + "/files/groups.txt";
        ID = idprefix;
        ScriptType = type;
        testsets = new TreeMap<>();
        parse(languageProps, executableProps);
    }

    public void parse(Properties languageProps, Properties executableProps) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(XMLpath);
        groupstxt = null;
        if ((new File(GroupsPath)).exists()) {
            groupstxt = new BufferedReader(new FileReader(GroupsPath));
        }
        //NodeList problem = doc.getDocumentElement().getChildNodes();
        Element el = doc.getDocumentElement();
        shortName = el.getAttribute("short-name");

        System.out.println("\nparsing problem '" + shortName + "'");

        url = el.getAttribute("url");
        if (ID.startsWith("com.codeforces.polygon") || ID.equals("auto")) {
            String[] t = url.split("/");
            String cflogin = t[t.length - 2];
            if (cflogin.contains(".")) {
                System.out.println("WARNING: Problem owner login contains '.', replacing with '-'");
                cflogin = cflogin.replaceAll("\\.", "-");
            }
            ID = "com.codeforces.polygon." + cflogin;
        }
        ID = ID + "." + shortName;

        //names
        NodeList nl = ((Element) doc.getElementsByTagName("names").item(0)).getElementsByTagName("name");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            el = (Element) n;
            if (el.getAttribute("language").equals("russian")) {
                Name = el.getAttribute("value");
                System.out.println("problem name = '" + Name + "'");
            } else {
                System.out.println(el.getAttribute("language"));
            }
        }

        //judging
        el = (Element) doc.getElementsByTagName("judging").item(0);
        input = el.getAttribute("input-file");
        if (input.isEmpty()) input = "*";
        output = el.getAttribute("output-file");
        if (output.isEmpty()) output = "*";

        //testset
        nl = el.getElementsByTagName("testset");
        for (int i = 0; i < nl.getLength(); i++) {//testset
            //System.out.println("DEBUG: testsets cnt = " + nl.getLength() + " i = " + i);
            el = (Element) nl.item(i);
            Testset ts = new Testset();
            ts.parse(this, el);
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

        //parse points and groups
        if (testsets.get("tests").groups.size() != 0) {
            ArrayList<Group> gg = testsets.get("tests").groups;
            Group.parse(groupstxt, gg);
            for (int i = 0; i < gg.size(); i++) {
                gg.get(i).parseIntPoints();
            }
        }
        //files attachments
        el = (Element) ((Element) doc.getElementsByTagName("files").item(0)).getElementsByTagName("attachments").item(0);
        attachments = new ArrayList<>();
        if (el != null) {
            nl = el.getElementsByTagName("file");
            for (int i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                Attachment attach = Attachment.parse(this, el, languageProps);
                if (attach != null) {
                    attachments.add(attach);
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
    }

    private Interactor handleInteractor(Document doc) throws IOException {
        Element interactorNode = (Element) ((Element) doc.getElementsByTagName("assets").item(0)).
                getElementsByTagName("interactor").item(0);
        if (interactorNode == null) {
            return null;
        }
        Element el = (Element) interactorNode.getElementsByTagName("source").item(0);
        String sourcePath = el.getAttribute("path");
        String sourceType = el.getAttribute("type");
        el = (Element) interactorNode.getElementsByTagName("binary").item(0);
        String binaryPath = el == null ? null : el.getAttribute("path");
        FileUtils.copyFile(new File(problemDirectory, sourcePath), new File(problemDirectory, "interact.cpp"));
        if (binaryPath != null) {
            FileUtils.copyFile(new File(problemDirectory, binaryPath), new File(problemDirectory, "interact.exe"));
        }
        if (!sourceType.startsWith("cpp")) {
            System.err.println("WARNING: Only C++ interactors are supported, interact.cpp and [interact.exe] are created");
        }
        return new Interactor("x86.exe.win32", "interact.exe");
    }

    public void print(PrintWriter pw) {
        pw.println("<?xml version = \"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<problem");
        pw.println("\tversion = \"1.0\"");
        pw.println("\tid = \"" + ID + "\"");
        pw.println(">");
        pw.println("\t<judging>");

        pw.println("\t\t<script type = \"%" + "icpc" + "\">");
        for (Map.Entry<String, Testset> t: testsets.entrySet()){
            if (!t.getValue().name.equals("preliminary")) {
                t.getValue().print(pw, "\t\t\t", "icpc");
            }
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
        for (Map.Entry<String, Testset> t: testsets.entrySet()){
            t.getValue().print(pw, "\t\t\t", "ioi");
        }
        verifier.print(pw, "\t\t\t");
        for (Attachment at : attachments) {
            at.print(pw, "\t\t\t");
        }
        pw.println("\t\t</script>");

        pw.println("\t</judging>");
        pw.println("</problem>");
    }

    public boolean copyToVFS(String vfs, BufferedReader in, boolean update) throws IOException {
        File src = (new File(XMLpath)).getParentFile();
        File dest = new File(vfs + "/problems/" + ID.replaceAll("\\.", "/"));
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
