package pcms2;

import java.io.PrintWriter;

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
        }
        pw.println(tabs + "<test-group");
        pw.println(tabs + "\tcomment =\"" + comment + commentname + "\"");
        pw.println(tabs + "\tscoring =\"" + scoring + "\"");
        pw.println(tabs + "\tfeedback =\"" + feedback + "\"");
        pw.println(tabs + "\tgroup-bonus =\"" + groupBonus + "\"");
        pw.println(tabs + "\trequire-groups =\"" + requireGroups + "\"");
        pw.println(tabs + ">");
    }

    public void parseIntPoints() {
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
        String[] p = res.split(" ");
        intPoints = new int[p.length];
        for (int i = 0; i < p.length; i++){
            intPoints[i] = Integer.parseInt(p[i]);
        }
    }
}
