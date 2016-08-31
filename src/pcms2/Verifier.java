package pcms2;

import java.io.PrintWriter;

/**
 * Created by Ilshat on 11/23/2015.
 */
public class Verifier {
    public String type;
    public String executableId;
    public String file;

    public void print(PrintWriter pw, String tabs) {
        pw.println(tabs + "<verifier type=\"%" + type + "\">");
        if (executableId.equals("exe.win32")) {
            executableId = "x86." + executableId;
        }
        if (executableId.equals("jar7") || executableId.equals("jar8")) {
            executableId = "java.check";
        }
        pw.println(tabs + "\t<binary executable-id =\"" + executableId + "\" file =\"" + file + "\" />");
        pw.println(tabs + "</verifier>");
    }
}
