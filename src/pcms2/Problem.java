package pcms2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
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
    TreeMap <String, Testset> testsets;
    ArrayList<Attachment> attachments;
    Verifier v;
    boolean hasPreliminary = false;

    public Problem(String path, String idprefix, String type) throws Exception {
        XMLpath = path + "/problem.xml";
        GroupsPath = path + "/files/groups.txt";
        ID = idprefix;
        ScriptType = type;
        testsets = new TreeMap<>();
        parse();
    }

    public void parse() throws Exception {
        System.out.println("parsing problem...");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(XMLpath);
        BufferedReader groupstxt = null;
        if ((new File(GroupsPath)).exists()) {
            groupstxt = new BufferedReader(new FileReader(GroupsPath));
        }
        //NodeList problem = doc.getDocumentElement().getChildNodes();
        Element el = doc.getDocumentElement();
        shortName = el.getAttribute("short-name");
        url = el.getAttribute("url");
        if (ID.startsWith("com.codeforces.polygon")) {
            String[] t = url.split("/");
            ID = "com.codeforces.polygon." + t[t.length - 2];
        }
        ID = ID + "." + shortName;


        //names
        NodeList nl = ((Element) doc.getElementsByTagName("names").item(0)).getElementsByTagName("name");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            el = (Element) n;
            if (el.getAttribute("language").equals("russian")) {
                Name = el.getAttribute("value");
                System.out.println(Name);
            } else {
                System.out.println(el.getAttribute("language"));
            }
        }
        //judging
        el = (Element) doc.getElementsByTagName("judging").item(0);
        String inp = el.getAttribute("input-file");
        if (inp.isEmpty()) inp = "*";
        String outp = el.getAttribute("output-file");
        if (outp.isEmpty()) outp = "*";
        nl = el.getElementsByTagName("testset");
        int sampleCount = 0;
        for (int i = 0; i < nl.getLength(); i++) {//testset
            //System.out.println("DEBUG: testsets cnt = " + nl.getLength() + " i = " + i);
            boolean isPreliminary = false;
            boolean hasGroups = false;
            el = (Element) nl.item(i);
            Testset ts = new Testset();
            TreeSet<String> gmap = new TreeSet<>();
            ts.name = el.getAttribute("name");
            ts.inputName = inp;
            ts.outputName = outp;
            ts.timeLimit = Double.parseDouble(el.getElementsByTagName("time-limit").item(0).
                    getChildNodes().item(0).getNodeValue()) / 1000;
            ts.memoryLimit = el.getElementsByTagName("memory-limit").item(0).
                    getChildNodes().item(0).getNodeValue();
            int tc = Integer.parseInt(el.getElementsByTagName("test-count").item(0).
                    getChildNodes().item(0).getNodeValue());
            ts.inputHref = el.getElementsByTagName("input-path-pattern").item(0).
                    getChildNodes().item(0).getNodeValue();
            ts.outputHref = el.getElementsByTagName("answer-path-pattern").item(0).
                    getChildNodes().item(0).getNodeValue();

            if (ts.name.equals("preliminary")) {
                hasPreliminary = true;
                isPreliminary = true;
            }

            NodeList nl1 = el.getElementsByTagName("tests");
            nl1 = ((Element) nl1.item(0)).getElementsByTagName("test");
            ts.tests = new Test[tc];
            //System.out.println("test count = " + tc);
            for (int j = 0; j < nl1.getLength(); j++) {//tests
                //System.out.println("DEBUG: j = " + j);
                el = (Element) nl1.item(j);
                String cm = el.getAttribute("method");
                String g = "-1";
                if (!el.getAttribute("cmd").isEmpty()) {
                    cm += " cmd: '" + el.getAttribute("cmd") + "'";
                }
                if (el.getAttribute("sample").equals("true")) {
                    if (isPreliminary) {
                        g = "sample";
                    }
                    if (!hasPreliminary && ScriptType.equals("ioi")) {
                        sampleCount++;
                    }

                }
                if (!el.getAttribute("group").isEmpty()) {
                    hasGroups = true;
                    g = el.getAttribute("group");
                    if (gmap.contains(g)) {
                        Group gg = ts.groups.get(gmap.size() - 1);
                        gg.last += 1;
                    } else {
                        gmap.add(g);
                        Group gg = new Group();

                        if (ts.name.equals("tests") && groupstxt != null) {
                            String[] group_params = groupstxt.readLine().trim().split("\t");
                            System.out.println("INFO: " +
                                    "Group parameters:'" + Arrays.toString(group_params) + "'. " +
                                    "Group: '" + g + "' " +
                                    "First test: " + j);

                            for (int ig = 0; ig < group_params.length; ig++) {
                                String[] kv = getKeyAndValue(group_params[ig]);
                                if (kv[0].equals("group")) {
                                    if (Integer.parseInt(kv[1]) != ts.groups.size()) {
                                        System.out.println("WARNING: Group numbers are not consecutive? " +
                                                "Group parameters:'" + Arrays.toString(group_params) + "'. " +
                                                "Group: '" + g + "'");
                                    }
                                } else if (kv[0].equals("group-bonus")) {
                                    gg.groupBonus = kv[1];
                                } else if (kv[0].equals("require-groups")) {
                                    String[] grps = kv[1].split(" ");
                                    gg.requireGroups = "";
                                    for (String grp : grps) {
                                        try {
                                            int abc = Integer.parseInt(grp);
                                            abc++;
                                            gg.requireGroups += "" + abc + " ";
                                        } catch (NumberFormatException e) {
                                            continue;
                                        }
                                    }
                                    //gg.requireGroups = kv[1];
                                } else if (kv[0].equals(("feedback"))) {
                                    gg.feedback = kv[1];
                                } else if (kv[0].equals("points")) {
                                    gg.points = kv[1];
                                    gg.parseIntPoints();
                                } else if (kv[0].equals("comment")) {
                                    gg.commentname = ". " + kv[1];
                                } else {
                                    System.out.println("WARNING: unknown parameter in groups.txt");
                                }
                            }
                        }
                        gg.first = j;
                        gg.last = j;
                        gg.comment = g;
                        ts.groups.add(gg);
                    }
                } else if (hasGroups) {
                    System.out.println("WARNING: Groups are enabled but test '" + j + "' has no group!");
                }

                if (!g.equals("-1")) {
                    Group gg = ts.groups.get(gmap.size() - 1);
                    if (j >= gg.first && j <= gg.last) {
                        if (gg.intPoints != null) {
                            ts.tests[j] = new Test("" + gg.intPoints[j - gg.first], cm, g);
                        }
                    } else {
                        System.out.println("WARNING: Could not get right group for test '" + j + "'!");
                    }
                }
                if (ts.tests[j] == null) {
                    ts.tests[j] = new Test("0", cm, g);
                }
                //System.out.println(ts.tests[j].comment + " " + ts.tests[j].points + " " + ts.tests[j].group);
            }
            testsets.put(ts.name, ts);
            //System.out.println("testset finished");
        }
        if (!hasPreliminary && ScriptType.equals("ioi")) {
            System.out.println("No preliminary testset, getting sample tests");
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
        //files attachments
        el = (Element) ((Element) doc.getElementsByTagName("files").item(0)).getElementsByTagName("attachments").item(0);
        attachments = new ArrayList<>();
        if (el != null) {
            nl = el.getElementsByTagName("file");

            for (int i = 0; i < nl.getLength(); i++) {
                el = (Element) nl.item(i);
                String atpath = el.getAttribute("path");
                String ext = atpath.substring(atpath.lastIndexOf('.') + 1, atpath.length());
                String fname = atpath.substring(atpath.lastIndexOf("/") + 1, atpath.lastIndexOf('.'));
                System.out.println("File name is '" + fname + "'");
                if (fname.equals(shortName) && !ext.equals("h")) {
                    System.out.println("Skipping solution stub '" + fname + "." + ext + "'");
                    continue;
                }
                Attachment attach = new Attachment();
                attach.href = atpath;
                if (ext.equals("h")) {
                    if (fname.endsWith("_c")) {
                        attach.languageId = "c.gnu";
                    } else {
                        attach.languageId = "cpp.gnu";
                    }
                } else if (ext.equals("cpp")) {
                    attach.languageId = "cpp.gnu";
                } else if (ext.equals("c")) {
                    attach.languageId = "c.gnu";
                } else if (ext.equals("pas")) {
                    attach.languageId = "pascal.free";
                } else if (ext.equals("java")) {
                    attach.languageId = "java";
                } else {
                    attach = null;
                }
                if (attach != null) {
                    attachments.add(attach);
                }
            }
        }
        //assets (checker)
        el = (Element) ((Element) doc.getElementsByTagName("assets").item(0)).getElementsByTagName("checker").item(0);
        v = new Verifier();
        v.type = el.getAttribute("type");
        el = (Element) el.getElementsByTagName("binary").item(0);
        v.executableId = el.getAttribute("type");
        v.file = el.getAttribute("path");
        //shortName = problem.getAttributes().getNamedItem("short-name").getNodeValue();
    }

    public void print(PrintWriter pw) {
        pw.println("<?xml version = \"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<problem");
        pw.println("\tversion = \"1.0\"");
        pw.println("\tid = \"" + ID + "\"");
        pw.println(">");
        pw.println("\t<judging>");
        pw.println("\t\t<script type = \"%" + ScriptType + "\">");
        for (Map.Entry<String, Testset> t: testsets.entrySet()){
            t.getValue().print(pw, "\t\t\t", ScriptType);
        }

        v.print(pw, "\t\t\t");
        for (Attachment at : attachments) {
            at.print(pw, "\t\t\t");
        }
        pw.println("\t\t</script>");
        pw.println("\t</judging>");
        pw.println("</problem>");
    }

    String[] getKeyAndValue(String s) {
        //key="value"
        String[] ss = s.split("=");
        ss[0] = ss[0].trim();
        ss[1] = ss[1].trim();
        ss[1] = ss[1].substring(1, ss[1].length() - 1);
        return ss;
    }
}
