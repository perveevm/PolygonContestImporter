package pcms2;

import polygon.Checker;

import java.io.PrintWriter;
import java.util.Properties;

/**
 * Created by Ilshat on 11/23/2015.
 */
public class Verifier {
    String type;
    String executableId;
    String file;

    public void print(PrintWriter pw, String tabs) {
        pw.println(tabs + "<verifier type = \"%" + type + "\">");
        pw.println(tabs + "\t<binary executable-id = \"" + executableId + "\" file = \"" + file + "\" />");
        pw.println(tabs + "</verifier>");
    }

    public static Verifier parse(Checker checker, Properties executableProps) {
        Verifier v = new Verifier();
        v.type = checker.getType();
        v.executableId = executableProps.getProperty(checker.getBinaryType());
        v.file = checker.getBinaryPath();
        return v;
    }
}
