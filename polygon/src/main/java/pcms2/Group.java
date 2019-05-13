package pcms2;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pcms2.properties.Feedback;
import pcms2.properties.Scoring;
import polygon.properties.FeedbackPolicy;
import polygon.properties.PointsPolicy;

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
//    String name;
    String comment = "";
    Scoring scoring = Scoring.GROUP;
    Feedback feedback = Feedback.GROUP_SCORE_AND_TEST;
    int groupBonus;
    String requireGroups = "";
    String points;
//    double pointsSum = 0;
//    int intPoints[];
    int first = -1;
    int last = -1;
    int testCount = 0;
    boolean hasSampleTests;

    public void println(PrintWriter pw, String tabs) {
        pw.println(tabs + "<test-group");
        pw.println(tabs + "\tcomment = \"" + comment + "\"");
        pw.println(tabs + "\tscoring = \"" + scoring + "\"");
        pw.println(tabs + "\tfeedback = \"" + feedback + "\"");
        pw.println(tabs + "\tgroup-bonus = \"" + groupBonus + "\"");
        pw.println(tabs + "\trequire-groups = \"" + requireGroups + "\"");
        pw.println(tabs + ">");
    }

//    public void parseIntPoints() {
//        if (points == null || points.isEmpty()) {
//            return;
//        }
////        System.out.println("DEBUG: parsing points '" + points + "'");
//        int[] p = getNumbersArray(points);
////        System.out.println("DEBUG: points parsed " + Arrays.toString(p));
//        int nonZero = 0;
//        for (int i = 0; i < p.length; i++) {
//            if (p[i] > 0) nonZero++;
//        }
//        if (nonZero == 0) {
//            return;
//        }
//
//        if (p.length == 1 || scoring == Scoring.SUM && nonZero != p.length) {
//            int tcount = last - first + 1;
//            int total = Math.max(p[0], (int) pointsSum);
//
//            if (total < tcount) {
//                System.out.println("WARNING: Could not parse 'points' parameter for group '" + name + "'. Tests count is bigger than points.");
//                return;
//            }
//
//            intPoints = new int[tcount];
//            for (int i = 0; i < tcount - total % tcount; i++) {
//                intPoints[i] = total / tcount;
//            }
//            for (int i = tcount - total % tcount; i < tcount; i++) {
//                intPoints[i] = total / tcount + 1;
//            }
//            return;
//        }
//        if (last - first + 1 == p.length) {
//            intPoints = p;
//            return;
//        }
//
//        System.out.println("WARNING: Could not parse 'points' parameter for group '" + name + "'");
//    }

    public static Group parse(polygon.Group polygonGroup, Map<String, Integer> groupNameToId, boolean hasSampleTests, int firstTest, int lastTest) {
        String name = polygonGroup.getName();
        Group group = new Group();

        group.comment = name;
        group.first = firstTest;
        group.last = lastTest;
        group.hasSampleTests = hasSampleTests;

        PointsPolicy pointsPolicy = polygonGroup.getPointsPolicy();
        FeedbackPolicy feedbackPolicy = polygonGroup.getFeedbackPolicy();
        if (pointsPolicy != null && feedbackPolicy != null) {
            if (polygonGroup.getDependencies() != null) {
                for (String dep : polygonGroup.getDependencies()) {
                    group.requireGroups += groupNameToId.get(dep) + " ";
                }
            }

            group.scoring = Scoring.parse(pointsPolicy);
            group.feedback = Feedback.parse(feedbackPolicy);

            if (group.feedback == Feedback.OUTCOME && group.scoring == Scoring.GROUP) {
                group.scoring = Scoring.SUM;
            }

            if (pointsPolicy == PointsPolicy.COMPLETE_GROUP) {
                group.groupBonus = (int) polygonGroup.getPoints();
                if (group.groupBonus != polygonGroup.getPoints()) {
                    System.out.printf("WARNING: PCMS does not support non integer points, but group '%s' has '%f' points! Casting to int\n", polygonGroup.getName(), polygonGroup.getPoints());
                }
            }
        }



        TreeMap <String, String> parameters = polygonGroup.getParameters();
        if (parameters != null) {
            String param = parameters.get("feedback");
            if (param != null) {
                group.feedback = Feedback.getFeedback(param);
            }

            param = parameters.get("scoring");
            if (param != null) {
                group.scoring = Scoring.getScoring(param);
            }

            param = parameters.get("group-bonus");
            if (param != null) {
                group.groupBonus = Integer.parseInt(param);
            }

            param = parameters.get("points");
            if (param != null) {
                group.points = param;
            }

            param = parameters.get("comment");
            if (param != null) {
                group.comment = param;
            }

            param = parameters.get("require-groups");
            if (param != null) {
                int[] grps = getNumbersArray(param);
                group.requireGroups = "";
                for (int grp : grps) {
                    group.requireGroups += "" + (grp + 1) + " ";
                }
            }
        }

        if (hasSampleTests) {
            System.out.printf("INFO: Group '%s' contains sample tests, changing feedback to statistics!\n", group.comment);
            if (groupNameToId.put("Sample tests", groupNameToId.get(group.comment)) != null) {
                throw new AssertionError("ERROR: More than one group contains sample tests! Exiting\n");
            }
            group.comment = "Sample tests";
            group.scoring = Scoring.SUM;
            group.feedback = Feedback.STATISTICS;
            group.groupBonus = 0;
            group.requireGroups = "";

        }
        return group;
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
        sb.append("Group comment: ").append(comment).append("\n");

        sb.append("scoring: ").append(scoring).append("\n");
        sb.append("feedback: ").append(feedback).append("\n");
        if (groupBonus != 0) {
            sb.append("group-bonus: ").append(groupBonus).append("\n");
        }
        if (!requireGroups.isEmpty()) {
            sb.append("require-groups: ").append(requireGroups).append("\n");
        }
        if (!points.isEmpty()) {
            sb.append("points: ").append(points).append("\n");
        }
        if (points != null) {
            sb.append("points: ").append(points).append("\n");
        }
        if (first != -1 && last != -1) {
            sb.append("first: ").append(first).append(", last: ").append(last).append("\n");
        }
        return sb.toString();
    }
}
