package pcms2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Problem {
    String XMLpath;
    String ID;
    String ScriptType;
    String Name;
    String shortName;
    String url;
    Testset[] testsets;
    Verifier v;
    boolean hasPreliminary = false;

    public Problem(String path, String idprefix, String type) throws Exception {
        XMLpath = path;
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
        //NodeList problem = doc.getDocumentElement().getChildNodes();
        Element el = doc.getDocumentElement();
        shortName = el.getAttribute("short-name");
        ID = ID + "." + shortName;
        url = el.getAttribute("url");

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
        pw.println("\t\t</script>");
        pw.println("\t</judging>");
        pw.println("</problem>");
    }
}
