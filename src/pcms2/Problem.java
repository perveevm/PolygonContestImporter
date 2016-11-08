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
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

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
    Testset[] testsets;
    ArrayList<Attachment> attachments;
    Verifier v;
    boolean hasPreliminary = false;

    public Problem(String path, String idprefix, String type) throws Exception {
        XMLpath = path + "/problem.xml";
        GroupsPath = path + "/files/groups.txt";
        ID = idprefix;
        ScriptType = type;
        if (ScriptType.equals("ioi")) {
            testsets = new Testset[2];
        } else {
            testsets = new Testset[1];
        }
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
            //System.out.println("testsets cnt = " + nl.getLength() + " i = " + i);
            boolean isPreliminary = false;
            boolean hasGroups = false;
            el = (Element) nl.item(i);
            Testset ts = new Testset();
            TreeSet<String> gmap = new TreeSet<>();
            ts.Name = el.getAttribute("name");
            ts.InputName = inp;
            ts.OutputName = outp;
            ts.TimeLimit = Double.parseDouble(el.getElementsByTagName("time-limit").item(0).
                    getChildNodes().item(0).getNodeValue()) / 1000;
            ts.MemoryLimit = el.getElementsByTagName("memory-limit").item(0).
                    getChildNodes().item(0).getNodeValue();
            int tc = Integer.parseInt(el.getElementsByTagName("test-count").item(0).
                    getChildNodes().item(0).getNodeValue());
            ts.InputHref = el.getElementsByTagName("input-path-pattern").item(0).
                    getChildNodes().item(0).getNodeValue();
            ts.OutputHref = el.getElementsByTagName("answer-path-pattern").item(0).
                    getChildNodes().item(0).getNodeValue();

            if (ts.Name.equals("preliminary")) {
                hasPreliminary = true;
                isPreliminary = true;
            } else {
                if (!hasPreliminary && i == 0 && ScriptType.equals("ioi")) {
                    testsets[0] = new Testset();
                    testsets[0].Name = "preliminary";
                    testsets[0].InputName = ts.InputName;
                    testsets[0].OutputName = ts.OutputName;
                    testsets[0].TimeLimit = ts.TimeLimit;
                    testsets[0].MemoryLimit = ts.MemoryLimit;
                    testsets[0].InputHref = ts.InputHref;
                    testsets[0].OutputHref = ts.OutputHref;
                    i++;
                    testsets[0].Tests = new Test[10];//to-do
                }
            }
            NodeList nl1 = el.getElementsByTagName("tests");
            nl1 = ((Element) nl1.item(0)).getElementsByTagName("test");
            ts.Tests = new Test[tc];
            //System.out.println("test count = " + tc);
            for (int j = 0; j < nl1.getLength(); j++) {//tests
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
                        testsets[0].Tests[sampleCount] = new Test("0", cm, "sample");
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

                        if (groupstxt != null) {
                            String[] group_params = groupstxt.readLine().trim().split("\t");

                            for (int ig = 0; ig < group_params.length; ig++) {
                                String[] kv = getKeyAndValue(group_params[ig]);
                                if (kv[0].equals("group")) {
                                    if (Integer.parseInt(kv[1]) != ts.groups.size()) {
                                        System.out.println("WARNING: Group numbers are not consecutive?");
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

                ts.Tests[j] = new Test("0", cm, g);
                //System.out.println(ts.Tests[j].comment + " " + ts.Tests[j].points + " " + ts.Tests[j].group);
            }
            testsets[i] = ts;
            //System.out.println("testset finished");
        }
        if (!hasPreliminary && ScriptType.equals("ioi")) {
            System.out.println("No preliminary testset, getting sample tests");
            Test[] temp = new Test[sampleCount];
            for (int i = 0; i < sampleCount; i++) {
                temp[i] = testsets[0].Tests[i];
            }
            testsets[0].Tests = temp;
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
        for (int i = 0; i < testsets.length; i++) {
            testsets[i].print(pw, "\t\t\t", ScriptType);
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
