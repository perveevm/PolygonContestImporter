package pcms2;

import java.io.PrintWriter;

/**
 * Created by Ilshat on 7/27/2016.
 */
public class Attachment {
    String href = "";
    String languageId = "";

    public void print(PrintWriter pw, String tabs){
        pw.println(tabs + "<attachment language-id=\"" + languageId + "\" href=\"" + href + "\"/>");

    }
}
