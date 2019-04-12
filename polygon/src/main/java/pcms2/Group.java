package pcms2;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pcms2.properties.Feedback;
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
    Feedback feedback = Feedback.GROUP_SCORE_AND_TEST;
    String groupBonus = null;
    String requireGroups = "";
    String points = "";
    double pointsSum = 0;
    int intPoints[];
    int first = -1;
    int last = -1;

    public void println(PrintWriter pw, String tabs) {
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
        int nonZero = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] > 0) nonZero++;
        }
        if (nonZero == 0) {
            return;
        }

        if (p.length == 1 || scoring == Scoring.SUM && nonZero != p.length) {
            int tcount = last - first + 1;
            int total = Math.max(p[0], (int) pointsSum);

            if (total < tcount) {
                System.out.println("WARNING: Could not parse 'points' parameter for group '" + name + "'. Tests count is bigger than points.");
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
        if (last - first + 1 == p.length) {
            intPoints = p;
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
//            System.out.println("DEBUG: " + gg.toString());
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
                    gg.feedback = Feedback.getFeedback(entry.getValue());
                } else if (entry.getKey().equals("points")) {
                    gg.points = entry.getValue();
                } else if (entry.getKey().equals("comment")) {
                    gg.comment = entry.getValue();
                } else if (entry.getKey().equals("scoring")) {
                    gg.scoring = Scoring.getScoring(entry.getValue());
                } else if (entry.getKey().equals("group")) {
                    continue;
                } else {
                    System.out.println("WARNING: unknown parameter in groups.txt - '" + entry.getKey() + "'");
                }
            }
//            System.out.println("DEBUG: " + gg.toString());
        }
    }

    public static Group parse(Element groupElement, ArrayList<Group> groups, Map<String, Integer> groupNameToId) {
        String name = groupElement.getAttribute("name");
        Group group = groups.get(groupNameToId.get(name));
//        group.name = groupElement.getAttribute("name");
        String pointsPolicy = groupElement.getAttribute("points-policy");
        group.scoring = Scoring.parse(pointsPolicy);
        String feedbackPolicy = groupElement.getAttribute("feedback-policy");
        if (group.feedback != Feedback.STATISTICS) {
            group.feedback = Feedback.parse(feedbackPolicy);
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Group name: ").append(name).append("\n");
        if (!comment.isEmpty()) {
            sb.append("comment: ").append(comment).append("\n");
        }
        sb.append("scoring: ").append(scoring).append("\n");
        sb.append("feedback: ").append(feedback).append("\n");
        if (groupBonus != null) {
            sb.append("group-bonus: ").append(groupBonus).append("\n");
        }
        if (!requireGroups.isEmpty()) {
            sb.append("require-groups: ").append(requireGroups).append("\n");
        }
        if (!points.isEmpty()) {
            sb.append("points: ").append(points).append("\n");
        }
        if (pointsSum != 0) {
            sb.append("points-sum: ").append(pointsSum).append("\n");
        }
        if (intPoints != null) {
            sb.append("int-points: ").append(Arrays.toString(intPoints)).append("\n");
        }
        if (first != -1 && last != -1) {
            sb.append("first: ").append(first).append(", last: ").append(last).append("\n");
        }
        return sb.toString();
    }
}
