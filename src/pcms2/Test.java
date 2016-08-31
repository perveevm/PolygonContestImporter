package pcms2;

import java.io.PrintWriter;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Test {
    public String points;
    public String comment;
    public String group;

    public Test(String p, String c, String g) {
        points = p;
        comment = c;
        group = g;
    }

    public void println(PrintWriter pw, String tabs) {
        pw.println(tabs + "<test points=\"" + points + "\" comment=\"" + comment + "\" />");
    }
}
