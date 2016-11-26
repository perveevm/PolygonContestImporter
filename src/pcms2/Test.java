package pcms2;

import java.io.PrintWriter;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Test {
    public String comment;
    public String group;

    public Test(String c, String g) {
        comment = c;
        group = g;
    }

    public void println(PrintWriter pw, String tabs, String points) {
        pw.println(tabs + "<test points=\"" + points + "\" comment=\"" + comment + "\" />");
    }

    public void println(PrintWriter pw, String tabs) {
        pw.println(tabs + "<test comment=\"" + comment + "\" />");
    }
}
