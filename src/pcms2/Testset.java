package pcms2;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Testset {
    public String name;
    public String inputName;
    public String outputName;
    public String inputHref;
    public String outputHref;
    public double timeLimit;
    public String memoryLimit;
    public ArrayList<Group> groups;
    public Test[] tests;

    public Testset() {
        groups = new ArrayList<>();
    }

    public Testset(String name, String input_name,
                   String output_name,
                   String input_href,
                   String output_href,
                   int time_limit,
                   String memor_limit) {
        this.name = name;
        outputName = output_name;
        inputName = input_name;
        inputHref = input_href;
        outputHref = output_href;
        timeLimit = time_limit;
        memoryLimit = memor_limit;
        groups = new ArrayList<>();
    }

    public String formatHref(String in) {
        int begi = in.indexOf("%");
        int endi = in.indexOf("d");
        String tt = in.substring(begi + 1, endi);
        String ttt = "";
        for (int j = 0; j < Integer.parseInt(tt); j++) ttt += "#";

        return in.replace("%" + tt + "d", ttt);
    }
    public void print(PrintWriter pw, String tabs, String type){
        //TODO: Get rid of preliminary testset
        //if (name.equals("preliminary")) {
            //return;
        //}
        if (tests.length == 0) {
            System.out.println(String.format("WARNING: Testset %s contains zero tests, skipped", name));
            return;
        }
        pw.println(tabs + "<testset");
        if (!name.equals("preliminary")){
            name = "main";
        }
        if (type.equals("ioi")){
            pw.println(tabs + "\tname = \"" + name + "\"");
            if (name.equals("main") && groups.size() == 0){
                pw.println(tabs + "\tfeedback = \"outcome\"");
            }

        }
        pw.println(tabs + "\tinput-name = \"" + inputName + "\"");
        pw.println(tabs + "\toutput-name = \"" + outputName + "\"");
        pw.println(tabs + "\tinput-href = \"" + formatHref(inputHref) + "\"");
        pw.println(tabs + "\tanswer-href = \"" + formatHref(outputHref) + "\"");
        pw.println(tabs + "\ttime-limit = \"" + timeLimit + "s\"");
        pw.println(tabs + "\tmemory-limit = \"" + memoryLimit + "\"");
        if (type.equals("icpc")) {
            pw.println(tabs + "\ttest-count = \"" + tests.length + "\"");
        }
        pw.println(tabs + ">");

        if (type.equals("ioi")) {
            if (name.equals("preliminary")) {
                for (int i = 0; i < tests.length; i++) {
                    tests[i].println(pw, tabs + "\t");
                }
            } else {
                int g = groups.size();
                if (g == 0) {
                    for (int i = 0; i < tests.length; i++) {
                        tests[i].println(pw, tabs + "\t");
                    }
                } else {
                    for (int i = 0; i < g; i++) {
                        groups.get(i).println(pw, tabs + "\t");
                        for (int j = groups.get(i).first; j <= groups.get(i).last; j++) {
                            if (groups.get(i).intPoints == null) {
                                tests[j].println(pw, tabs + "\t\t");
                            } else {
                                tests[j].println(pw, tabs + "\t\t", "" + groups.get(i).intPoints[j - groups.get(i).first]);
                            }
                        }
                        pw.println(tabs + "\t</test-group>");
                    }
                }

            }
        }
        pw.println(tabs + "</testset>");
    }

    public void parse(Problem problem, Element el) throws IOException {
        //System.out.println("DEBUG: testsets cnt = " + nl.getLength() + " i = " + i);
        boolean isPreliminary = false;
        boolean hasGroups = false;

        TreeSet<String> gmap = new TreeSet<>();
        name = el.getAttribute("name");
        inputName = problem.input;
        outputName = problem.output;
        timeLimit = Double.parseDouble(el.getElementsByTagName("time-limit").item(0).
                getChildNodes().item(0).getNodeValue()) / 1000;
        memoryLimit = el.getElementsByTagName("memory-limit").item(0).
                getChildNodes().item(0).getNodeValue();
        int tc = Integer.parseInt(el.getElementsByTagName("test-count").item(0).
                getChildNodes().item(0).getNodeValue());
        inputHref = el.getElementsByTagName("input-path-pattern").item(0).
                getChildNodes().item(0).getNodeValue();
        outputHref = el.getElementsByTagName("answer-path-pattern").item(0).
                getChildNodes().item(0).getNodeValue();

        if (name.equals("preliminary")) {
            problem.hasPreliminary = true;
            isPreliminary = true;
        }

        NodeList nl1 = el.getElementsByTagName("tests");
        nl1 = ((Element) nl1.item(0)).getElementsByTagName("test");
        tests = new Test[tc];
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
                if (!problem.hasPreliminary) {
                    problem.sampleCount++;
                }

            }
            if (!el.getAttribute("group").isEmpty()) {
                hasGroups = true;
                g = el.getAttribute("group");
                if (gmap.contains(g)) {
                    Group gg = groups.get(gmap.size() - 1);
                    gg.last += 1;
                } else {
                    gmap.add(g);
                    Group gg = new Group();
                    gg.first = j;
                    gg.last = j;
                    gg.comment = g;
                    groups.add(gg);
                }
            } else if (hasGroups) {
                System.out.println("WARNING: Groups are enabled but test '" + j + "' has no group!");
            }

            tests[j] = new Test(cm, g);
            //System.out.println("DEBUG: " + ts.tests[j].comment + " " + ts.tests[j].points + " " + ts.tests[j].group);
        }

    }

}
