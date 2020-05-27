package pcms2;

import java.io.PrintWriter;
import java.util.Properties;

/**
 * Created by Niyaz Nigmatullin on 26.03.17.
 */
public class Interactor {
    final String executableId;
    final String binary;


    public Interactor(String executableId, String binary) {
        this.executableId = executableId;
        this.binary = binary;
    }

    public void print(PrintWriter writer, String tabs) {
        writer.println(tabs + "<interactor type = \"%testlib\">");
        writer.println(tabs + "\t<binary executable-id = \"" + executableId + "\" file = \"" + binary + "\" />");
        writer.println(tabs + "</interactor>");
    }

    public static Interactor parse(polygon.Interactor interactor, Properties executableProps) {
        if (interactor == null) {
            return null;
        }
        String binaryPath = interactor.getBinaryPath();
        String binaryType = interactor.getBinaryType();

        return new Interactor(executableProps.getProperty(binaryType), binaryPath);

    }
}
