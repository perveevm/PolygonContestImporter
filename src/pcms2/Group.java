package pcms2;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pcms2.properties.Scoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Ilshat on 11/23/2015.
 */
public class Group {
    String name;
    String comment = "";
    Scoring scoring = Scoring.SUM;
    String feedback = "group-score-and-test";
    String groupBonus = null;
    String requireGroups = "";
    String points = "";
    double pointsSum = 0;
    int intPoints[];
    int first = -1;
    int last = -1;

    public void println(PrintWriter pw, String tabs) {
        if (comment.isEmpty()) {
            comment = name;
        }
        if (groupBonus == null) {
            groupBonus = "" + (int) pointsSum;
        }
        pw.println(tabs + "<test-group");
        pw.println(tabs + "\tcomment = \"" + comment + "\"");
        pw.println(tabs + "\tscoring = \"" + scoring + "\"");
        pw.println(tabs + "\tfeedback = \"" + feedback + "\"");
        pw.println(tabs + "\tgroup-bonus = \"" + groupBonus + "\"");
        pw.println(tabs + "\trequire-groups = \"" + requireGroups + "\"");
        pw.println(tabs + ">");
    }

    public void parseIntPoints() {
        if (points == null || points.isEmpty()) {
            return;
        }
//        System.out.println("DEBUG: parsing points '" + points + "'");

        int[] p = getNumbersArray(points);
//        System.out.println("DEBUG: points parsed " + Arrays.toString(p));

        if (last - first + 1 == p.length) {
            intPoints = p;
            return;
        }

        if (p.length == 1) {
            int tcount = last - first + 1;
            int total = p[0];

            if (total < tcount) {
                System.out.println("WARNING: Could not parse 'points' parameter for group '" + name + "'");
                return;
            }

            intPoints = new int[tcount];
            for (int i = 0; i < tcount - total % tcount; i++) {
                intPoints[i] = total / tcount;
            }
            for (int i = tcount - total % tcount; i < tcount; i++) {
                intPoints[i] = total / tcount + 1;
            }
            return;
        }

        System.out.println("WARNING: Could not parse 'points' parameter for group '" + name + "'");
    }

    public static void parseGroupstxt(BufferedReader groupstxt, ArrayList<Group> groups, Map<String, Integer> groupNameToId) throws IOException {
        if (groupstxt == null) return;

        String line;
        while ((line = groupstxt.readLine()) != null) {
            line = line.trim();
            if (line == null || line.isEmpty()) continue;

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
            int group_id = groupNameToId.get(group_par.get("group"));

            if (group_id >= groups.size() || group_id < 0) {
                System.out.println("WARNING: Group id in 'groups.txt' is wrong! " +
                        "Group parameters:'" + Arrays.toString(group_params) + "'. ");
                continue;
            }

            Group gg = groups.get(group_id);
            System.out.println("INFO: " +
                    "Group parameters:'" + Arrays.toString(group_params) + "'. " +
                    "Group: '" + group_id + "' ");

            for (Map.Entry<String, String> entry : group_par.entrySet()) {
                if (entry.getKey().equals("group-bonus")) {
                    gg.groupBonus = entry.getValue();
                } else if (entry.getKey().equals("require-groups")) {
                    int[] grps = getNumbersArray(entry.getValue());
                    gg.requireGroups = "";
                    for (int grp : grps) {
                        gg.requireGroups += "" + (grp + 1) + " ";
                    }
                } else if (entry.getKey().equals(("feedback"))) {
                    gg.feedback = entry.getValue();
                } else if (entry.getKey().equals("points")) {
                    gg.points = entry.getValue();
                } else if (entry.getKey().equals("comment")) {
                    gg.comment = entry.getValue();
                } else if (entry.getKey().equals("scoring")) {
                    gg.scoring = Scoring.parse(entry.getValue());
                } else if (entry.getKey().equals("group")) {
                    continue;
                } else {
                    System.out.println("WARNING: unknown parameter in groups.txt - '" + entry.getKey() + "'");
                }
            }
        }
    }

    public static Group parse(Element groupElement, Map <String, Integer> groupNameToId) {
        Group group = new Group();
        group.name = groupElement.getAttribute("name");
        String pointsPolicy = groupElement.getAttribute("points-policy");
        if (pointsPolicy.equals("complete-group")) {
            group.scoring = Scoring.GROUP;
        } else if (pointsPolicy.equals("each-test")) {
            group.scoring = Scoring.SUM;
        }
        NodeList dependencies = groupElement.getElementsByTagName("dependencies");
        if (dependencies != null && dependencies.getLength() > 0) {
            dependencies = ((Element) dependencies.item(0))
                    .getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dep = (Element) dependencies.item(i);
                group.requireGroups += groupNameToId.get(dep.getAttribute("group")) + 1 + " ";
            }
        }
        return group;
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

    static int[] getNumbersArray(String s) {
        if (s == null || s.isEmpty()) {
            return new int[0];
        }
        int[] a = new int[s.length()];
        int last = 0;
        String res = "";
        boolean dig = false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= '0' && s.charAt(i) <= '9') {
                dig = true;
                res += s.charAt(i);
            } else {
                if (dig) {
                    try {
                        a[last] = Integer.parseInt(res);
                        last++;
                    } catch (NumberFormatException ignored) {
                    }
                    res = "";
                }
                dig = false;
            }
        }
        if (dig) {
            try {
                a[last] = Integer.parseInt(res);
                last++;
            } catch (NumberFormatException ignored) {
            }
        }
        return Arrays.copyOf(a, last);
    }
}
