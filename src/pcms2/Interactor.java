package pcms2;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Niyaz Nigmatullin on 26.03.17.
 */
public class Interactor {
    public final String executableId;
    public final String binary;

    public Interactor(String executableId, String binary) {
        this.executableId = executableId;
        this.binary = binary;
    }

    public void print(PrintWriter writer, String tabs) {
        writer.println(tabs + "<interactor type=\"%testlib\">");
        writer.println(tabs + "\t<binary executable-id =\"" + executableId + "\" file =\"" + binary + "\" />");
        writer.println(tabs + "</interactor>");
    }
}
