package pcms2;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Ilshat on 7/27/2016.
 */
public class Attachment {
    String href;
    String languageId;

    public void print(PrintWriter pw, String tabs){
        pw.println(tabs + "<attachment language-id=\"" + languageId + "\" href=\"" + href + "\"/>");

    }
    public static ArrayList<Attachment> parse(ArrayList<polygon.Attachment> attachments, Properties languagesProps, String shortName) {
        ArrayList<Attachment> result = new ArrayList<>();
        for (polygon.Attachment attachment : attachments) {
            String atpath = attachment.getPath();
            String ext = atpath.substring(atpath.lastIndexOf('.') + 1, atpath.length());
            String fname = atpath.substring(atpath.lastIndexOf("/") + 1, atpath.lastIndexOf('.'));
            //System.out.println("DEBUG: File name is '" + fname + "'");
            if (fname.equals("Solver") || (fname.equals(shortName) && !ext.equals("h"))) {
                System.out.println("Skipping solution stub '" + fname + "." + ext + "'");
                continue;
            }

            if (languagesProps.getProperty(ext) != null) {
                String[] langs = languagesProps.getProperty(ext).split(",");

                for (int i = 0; i < langs.length; i++) {
                    Attachment attach = new Attachment();
                    attach.href = atpath;
                    attach.languageId = langs[i].trim();
                    result.add(attach);
                }
            }
        }
        return result;
    }
}
