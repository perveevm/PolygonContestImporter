package pcms2;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pcms2.properties.Feedback;
import pcms2.properties.Scoring;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Testset {
    String name;
    String inputName;
    String outputName;
    String inputHref;
    String outputHref;
    double timeLimit;
    String memoryLimit;
    ArrayList<Group> groups;
    Map<String, Integer> groupNameToId;
    Test[] tests;

    public Testset() {
        groups = new ArrayList<>();
        groupNameToId = new HashMap<>();
    }

    public Testset(String name, String input_name,
                   String output_name,
                   String input_href,
                   String output_href,
                   int time_limit,
                   String memory_limit) {
        this.name = name;
        outputName = output_name;
        inputName = input_name;
        inputHref = input_href;
        outputHref = output_href;
        timeLimit = time_limit;
        memoryLimit = memory_limit;
        groups = new ArrayList<>();
        groupNameToId = new HashMap<>();
    }

    private String formatHref(String in) {
        int begi = in.indexOf("%");
        int endi = in.indexOf("d");
        String tt = in.substring(begi + 1, endi);
        String ttt = "";
        for (int j = 0; j < Integer.parseInt(tt); j++) ttt += "#";

        return in.replace("%" + tt + "d", ttt);
    }

    public void print(PrintWriter pw, String tabs, String type) {
        if (tests.length == 0) {
            System.out.println(String.format("WARNING: Testset %s contains zero tests, skipped", name));
            return;
        }
        pw.println(tabs + "<testset");
        if (!name.equals("preliminary")) {
            name = "main";
        }
        if (type.equals("ioi")) {
            pw.println(tabs + "\tname = \"" + name + "\"");
            if (name.equals("main") && groups.size() == 0) {
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
                for (Test test : tests) {
                    test.println(pw, tabs + "\t");
                }
            } else {
                int g = groups.size();
                if (g == 0) {
                    for (Test test : tests) {
                        test.println(pw, tabs + "\t", "" + ((int) test.points));
                    }
                } else {
                    for (Group group : groups) {

                        group.println(pw, tabs + "\t");
                        for (int j = group.first; j <= group.last; j++) {
                            if (group.intPoints == null || group.scoring == Scoring.GROUP) {
                                tests[j].println(pw, tabs + "\t\t");
                            } else {
                                tests[j].println(pw, tabs + "\t\t", "" + group.intPoints[j - group.first]);
                            }
                        }
                        pw.println(tabs + "\t</test-group>");
                    }
                }

            }
        }
        pw.println(tabs + "</testset>");
    }

    public static Testset parse(Problem problem, Element el) {
        //System.out.println("DEBUG: testsets cnt = " + nl.getLength() + " i = " + i);
        Testset ts = new Testset();

        boolean isPreliminary = false;
        boolean hasGroups = false;

        TreeSet<String> gmap = new TreeSet<>();
        ts.name = el.getAttribute("name");
        ts.inputName = problem.input;
        ts.outputName = problem.output;
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
            problem.hasPreliminary = true;
            isPreliminary = true;
        }

        //tests
        NodeList nl1 = el.getElementsByTagName("tests");
        nl1 = ((Element) nl1.item(0)).getElementsByTagName("test");
        ts.tests = new Test[tc];
        for (int j = 0; j < nl1.getLength(); j++) {//tests
            //System.out.println("DEBUG: j = " + j);
            Element testEl = (Element) nl1.item(j);
            String comment = testEl.getAttribute("method");
            String groupName = "-1";
            double points = 0;

            if (!testEl.getAttribute("cmd").isEmpty()) {
                comment += " cmd: '" + testEl.getAttribute("cmd") + "'";
            }

            if (!testEl.getAttribute("points").isEmpty()) {
                points = Double.parseDouble(testEl.getAttribute("points"));
                if (Double.compare(points, (int) points) != 0) {
                    System.out.println("WARNING: Non-integer points are not supported in PCMS but test '" + j + "' has non-integer points!");
                }
            }

            boolean sample = false;
            if (testEl.getAttribute("sample").equals("true")) {
                sample = true;
                if (isPreliminary) {
                    groupName = "sample";
                }
                if (!problem.hasPreliminary) {
                    problem.sampleCount++;
                }

            }
            if (!testEl.getAttribute("group").isEmpty()) {
                hasGroups = true;
                groupName = testEl.getAttribute("group");
                if (!ts.groupNameToId.containsKey(groupName)) {
                    Group group = new Group();
                    group.first = j;
                    group.name = groupName;
                    ts.groups.add(group);
                    ts.groupNameToId.put(groupName, ts.groups.size() - 1);
                }
                Group gg = ts.groups.get(ts.groupNameToId.get(groupName));
                if (gg.first == -1) {
                    gg.first = j;
                }
                gg.last = j;
                gg.pointsSum += points;
                gg.points += ((int) points) + ",";
                if (sample && !gg.feedback.toString().equals("statistics")) {
                    System.out.printf("INFO: Group '%s' contains sample tests, changing feedback to statistics!\n", gg.name);
                    gg.feedback = Feedback.getFeedback("statistics");
                    gg.comment = "Sample tests";
                }
            } else if (hasGroups) {
                System.out.println("WARNING: Groups are enabled but test '" + j + "' has no group!");
            }

            ts.tests[j] = new Test(comment, groupName, points);
            //System.out.println("DEBUG: " + ts.tests[j].comment + " " + ts.tests[j].points + " " + ts.tests[j].group);
        }

        //groups
        NodeList groupsList = el.getElementsByTagName("groups");
        if (groupsList != null && groupsList.getLength() > 0) {
            groupsList = ((Element) groupsList.item(0)).getElementsByTagName("group");
            for (int i = 0; i < groupsList.getLength(); i++) {
                Element groupElement = (Element) groupsList.item(i);
                Group group = Group.parse(groupElement, ts.groups, ts.groupNameToId);
//                System.out.println("DEBUG: " + group.name + " " + groupElement.getNodeName());
//                System.out.printf("DEBUG: Group '%s', points = '%s'\n", group.name, group.points);
            }
        }
        return ts;
    }

}
