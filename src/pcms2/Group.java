package pcms2;

import java.io.PrintWriter;

/**
 * Created by Ilshat on 11/23/2015.
 */
public class Group {
    public String comment;
    public String score = "sum";
    public String feedback = "outcome";
    public String groupBonus = "0";
    public String requirePrevious = "false";
    public int first;
    public int last;
    public void println(PrintWriter pw, String tabs){
		if (comment.equals("0")){
			feedback = "statistics";
		}
        pw.println(tabs + "<test-group");
        pw.println(tabs + "\tcomment =\"" + comment + "\"");
        pw.println(tabs + "\tscore =\"" + score + "\"");
        pw.println(tabs + "\tfeedback =\"" + feedback + "\"");
        pw.println(tabs + "\tgroup-bonus =\"" + groupBonus + "\"");
        pw.println(tabs + "\trequire-previous =\"" + requirePrevious + "\"");
        pw.println(tabs + ">");
    }
}
