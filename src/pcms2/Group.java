package pcms2;

import com.sun.xml.internal.bind.v2.util.QNameMap;

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
    public String comment;
    public String commentname = "";
    public String scoring = "sum";
    public String feedback = "group-score-and-test";
    public String groupBonus = "0";
    public String requireGroups = "";
    public String points = "";
    public int intPoints[];
    public int first;
    public int last;

    public void println(PrintWriter pw, String tabs) {
        if (comment.equals("0")) {
            feedback = "statistics";
            commentname = ". Sample tests";
        }
        pw.println(tabs + "<test-group");
        pw.println(tabs + "\tcomment = \"" + comment + commentname + "\"");
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
        //System.out.println("DEBUG: parsing points '" + points + "'");
        String res = "";
        boolean dig = false;
        for (int i = 0; i < points.length(); i++){
            if (points.charAt(i) >= '0' && points.charAt(i) <= '9') {
                dig = true;
                res += points.charAt(i);
            } else {
                if (dig) {
                    res += ' ';
                }
                dig = false;
            }
        }
        res = res.trim();
        //System.out.println("DEBUG: res = " + res);
        String[] p = res.split(" ");

        if (last - first + 1 == p.length) {
            intPoints = new int[p.length];
            for (int i = 0; i < p.length; i++){
                intPoints[i] = Integer.parseInt(p[i]);
            }
            return;
        }
        if (p.length == 1){
            int tcount = last - first + 1;
            int total = Integer.parseInt(points);

            if (total < tcount) {
                System.out.println("WARNING: Could not parse 'points' parameter for group '" + comment + "'");
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

        System.out.println("WARNING: Could not parse 'points' parameter for group '" + comment + "'");
    }

    public static void parse(BufferedReader groupstxt, ArrayList<Group> groups) throws IOException {
        if (groupstxt == null) return;

        String line;
        while ((line = groupstxt.readLine()) != null) {
            line = line.trim();
            if (line == null || line.isEmpty()) continue;

            String[] group_params = line.split("(\t;)|(\t)|(;)");
            TreeMap <String, String> group_par = new TreeMap<>();
            for (int ig = 0; ig < group_params.length; ig++) {
                String[] kv = getKeyAndValue(group_params[ig]);
                group_par.put(kv[0], kv[1]);
            }
            if (!group_par.containsKey("group")) {
                System.out.println("WARNING: Group id was not found! " +
                        "Group parameters:'" + Arrays.toString(group_params) + "'. ");
                continue;
            }

            int group_id = Integer.parseInt(group_par.get("group"));
            if (group_id >= groups.size() || group_id < 0) {
                System.out.println("WARNING: Group id in 'groups.txt' is wrong! " +
                        "Group parameters:'" + Arrays.toString(group_params) + "'. ");
                continue;
            }
            Group gg = groups.get(group_id);
            System.out.println("INFO: " +
                    "Group parameters:'" + Arrays.toString(group_params) + "'. " +
                    "Group: '" + group_id + "' ");

            for (Map.Entry<String, String> entry: group_par.entrySet()){
                if (entry.getKey().equals("group-bonus")) {
                    gg.groupBonus = entry.getValue();
                } else if (entry.getKey().equals("require-groups")) {
                    String[] grps = entry.getValue().split(" ");
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
                } else if (entry.getKey().equals(("feedback"))) {
                    gg.feedback = entry.getValue();
                } else if (entry.getKey().equals("points")) {
                    gg.points = entry.getValue();
                } else if (entry.getKey().equals("comment")) {
                    gg.commentname = ". " + entry.getValue();
                } else if(entry.getKey().equals("scoring")) {
                    gg.scoring = entry.getValue();
                }else {
                    System.out.println("WARNING: unknown parameter in groups.txt");
                }
            }
        }
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
}
