package pcms2;

import polygon.properties.PointsPolicy;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

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
    //Maps group name to index, minimal index is 1
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

    private static String formatHref(String in) {
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
//        if (!name.equals("preliminary")) {
//            name = "main";
//        }
        if (type.equals("ioi")) {
            pw.println(tabs + "\tname = \"" + name + "\"");
            if (name.equals("main") && groups.size() == 0) {
                pw.println(tabs + "\tfeedback = \"outcome\"");
            }

        }
        pw.println(tabs + "\tinput-name = \"" + inputName + "\"");
        pw.println(tabs + "\toutput-name = \"" + outputName + "\"");
        pw.println(tabs + "\tinput-href = \"" + inputHref + "\"");
        pw.println(tabs + "\tanswer-href = \"" + outputHref + "\"");
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
                        test.println(pw, tabs + "\t", "" + test.points);
                    }
                } else {
                    for (Group group : groups) {

                        group.println(pw, tabs + "\t");
                        for (int j = group.first; j <= group.last; j++) {
                            if (tests[j].points == 0) {
                                tests[j].println(pw, tabs + "\t\t");
                            } else {
                                tests[j].println(pw, tabs + "\t\t", "" + tests[j].points);
                            }
                        }
                        pw.println(tabs + "\t</test-group>");
                    }
                }

            }
        }
        pw.println(tabs + "</testset>");
    }

    public static Testset parse(polygon.Testset polygonTestset, String input, String output, double multiplier) {
        //System.out.println("DEBUG: testsets cnt = " + nl.getLength() + " i = " + i);
        Testset ts = new Testset();

        boolean hasGroups = false;

        ts.name = polygonTestset.getName();
        if (ts.name.equals("tests")) {
            ts.name = "main";
        }
        ts.inputName = input;
        ts.outputName = output;
        ts.timeLimit = polygonTestset.getTimeLimit() / 1000 * multiplier;
        ts.memoryLimit = polygonTestset.getMemoryLimit();
        int tc = polygonTestset.getTestCount();
        ts.inputHref = formatHref(polygonTestset.getInputPathPattern());
        ts.outputHref = formatHref(polygonTestset.getOutputPathPattern());

        //tests
        ts.tests = new Test[tc];
        for (int j = 0; j < tc; j++) {//tests
            //System.out.println("DEBUG: j = " + j);
            polygon.Test polTest = polygonTestset.getTests()[j];
            String comment = polTest.getMethod();

            if (polTest.getCmd() != null) {
                comment += " cmd: '" + polTest.getCmd() + "'";
            }

            double points = 0;
            if (!polTest.getGroup().isEmpty()) {
                hasGroups = true;
                String groupName = polTest.getGroup();
                if (!ts.groupNameToId.containsKey(groupName)) {
                    Group group = new Group();
                    group.first = j;
                    group.comment = groupName;
                    ts.groups.add(group);
                    ts.groupNameToId.put(groupName, ts.groups.size());
                }
                Group gg = ts.groups.get(ts.groupNameToId.get(groupName) - 1);
                if (gg.first == -1) {
                    gg.first = j;
                }
                gg.last = j;
                gg.hasSampleTests = polTest.isSample();
                if (polTest.getPoints() > 0 && polygonTestset.getGroups().get(groupName).getPointsPolicy() == PointsPolicy.EACH_TEST) {
                    points = polTest.getPoints();
                    if (Double.compare(points, (int) points) != 0) {
                        System.out.println("WARNING: Non-integer points are not supported in PCMS but test '" + j + "' has non-integer points!");
                    }
                }
            } else if (hasGroups) {
                System.out.println("WARNING: Groups are enabled but test '" + j + "' has no group!");
            }

            ts.tests[j] = new Test(comment, (int) points);
            //System.out.println("DEBUG: " + ts.tests[j].comment + " " + ts.tests[j].points + " " + ts.tests[j].group);
        }

        //groups
        Map<String, polygon.Group> polGroups = polygonTestset.getGroups();
        if (polGroups != null) {
            for (polygon.Group polGroup : polGroups.values()) {
//                System.out.println("DEBUG: " + polGroup.getName());
                if (!ts.groupNameToId.containsKey(polGroup.getName())) {
                    System.out.printf("WARNING: No tests in group '%s'!\n", polGroup.getName());
                    continue;
                }
                int index = ts.groupNameToId.get(polGroup.getName());
                Group other = ts.groups.get(index - 1);
                Group group = Group.parse(polGroup, ts.groupNameToId, other.hasSampleTests, other.first, other.last);
                ts.groups.set(index - 1, group);
            }
        }
        return ts;
    }

}
