package pcms2;

import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.util.Properties;

/**
 * Created by Ilshat on 7/27/2016.
 */
public class Attachment {
    String href = "";
    String languageId = "";

    public void print(PrintWriter pw, String tabs){
        pw.println(tabs + "<attachment language-id=\"" + languageId + "\" href=\"" + href + "\"/>");

    }
    public static Attachment parse(Problem problem, Element el, Properties languagesProps) {
        String atpath = el.getAttribute("path");
        String ext = atpath.substring(atpath.lastIndexOf('.') + 1, atpath.length());
        String fname = atpath.substring(atpath.lastIndexOf("/") + 1, atpath.lastIndexOf('.'));
        //System.out.println("DEBUG: File name is '" + fname + "'");
        if (fname.equals("Solver") || (fname.equals(problem.shortName) && !ext.equals("h"))) {
            System.out.println("Skipping solution stub '" + fname + "." + ext + "'");
            return null;
        }
        Attachment attach = new Attachment();
        attach.href = atpath;
        if (languagesProps.getProperty(ext) != null) {
            String[] langs = languagesProps.getProperty(ext).split(",");
            for (String s: langs) {
                attach.languageId = s.trim();
            }
        } else {
            if (ext.equals("h")) {
                if (fname.endsWith("_c")) {
                    attach.languageId = "c.gnu";
                } else {
                    attach.languageId = languagesProps.getProperty("cpp.gnu");
                }
            } else if (ext.equals("cpp")) {
                attach.languageId = "cpp.gnu";
            } else if (ext.equals("c")) {
                attach.languageId = "c.gnu";
            } else if (ext.equals("pas")) {
                attach.languageId = "pascal.free";
            } else if (ext.equals("java")) {
                attach.languageId = "java";
            } else {
                attach = null;
            }
        }
        return attach;
    }
}
