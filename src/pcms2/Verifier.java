package pcms2;

import org.w3c.dom.Element;

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

    public static Verifier parse(Element el, Properties executableProps) {
        Verifier v = new Verifier();
        v.type = el.getAttribute("type");
        el = (Element) el.getElementsByTagName("binary").item(0);
        v.executableId = el.getAttribute("type");
        if (executableProps.getProperty(v.executableId) != null) {
            v.executableId = executableProps.getProperty(v.executableId);
        } else {
            if (v.executableId.equals("exe.win32")) {
                v.executableId = "x86." + v.executableId;
            }
            if (v.executableId.equals("jar7") || v.executableId.equals("jar8")) {
                v.executableId = "java.check";
            }
        }
        v.file = el.getAttribute("path");
        return v;
    }
}
