package pcms2;

import java.io.PrintWriter;

/**
 * Created by Ilshat on 11/23/2015.
 */
public class Group {
    public String comment;
    public String scoring = "group";
    public String feedback = "group-score-and-test";
    public String groupBonus = "0";
    public String requireGroups = "";
    public int first;
    public int last;

    public void println(PrintWriter pw, String tabs) {
        if (comment.equals("0")) {
            feedback = "statistics";
        }
        pw.println(tabs + "<test-group");
        pw.println(tabs + "\tcomment =\"" + comment + "\"");
        pw.println(tabs + "\tscoring =\"" + scoring + "\"");
        pw.println(tabs + "\tfeedback =\"" + feedback + "\"");
        pw.println(tabs + "\tgroup-bonus =\"" + groupBonus + "\"");
        pw.println(tabs + "\trequire-groups =\"" + requireGroups + "\"");
        pw.println(tabs + ">");
    }
}
