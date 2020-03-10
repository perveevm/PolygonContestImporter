package pcms2;

import org.apache.commons.io.FilenameUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Ilshat on 7/27/2016.
 */
public class Attachment {
    String href;
    String languageId;

    private Attachment(String href, String languageId) {
        this.href = href;
        this.languageId = languageId;
    }

    public void print(PrintWriter pw, String tabs){
        pw.println(tabs + "<attachment language-id=\"" + languageId + "\" href=\"" + href + "\"/>");

    }
    public static ArrayList<Attachment> parse(ArrayList<polygon.Attachment> attachments, Properties languagesProps, String shortName) {
        ArrayList<Attachment> result = new ArrayList<>();
        for (polygon.Attachment attachment : attachments) {
            String atpath = attachment.getPath();
            String atname = FilenameUtils.getName(atpath);
            String ext = FilenameUtils.getExtension(atname);
            String fname = FilenameUtils.getBaseName(atname);
            //System.out.println("DEBUG: File name is '" + fname + "'");
            if (fname.equals("Solver") || (fname.equals(shortName) && !ext.equals("h"))) {
                System.out.println("Skipping solution stub '" + fname + "." + ext + "'");
                continue;
            }

            if (languagesProps.getProperty(ext) != null) {
                String[] langs = languagesProps.getProperty(ext).split(",");

                for (String lang : langs) {
                    result.add(new Attachment(atpath, lang.trim()));
                }
            }
        }
        return result;
    }
}
