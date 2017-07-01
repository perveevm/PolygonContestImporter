package pcms2;

import org.w3c.dom.Element;
import java.io.PrintWriter;
import java.util.Properties;

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
        writer.println(tabs + "<interactor type = \"%testlib\">");
        writer.println(tabs + "\t<binary executable-id = \"" + executableId + "\" file = \"" + binary + "\" />");
        writer.println(tabs + "</interactor>");
    }

    public static Interactor parse(Element interactorNode, Properties executableProps) {
        if (interactorNode == null) {
            return null;
        }

        Element el = (Element) interactorNode.getElementsByTagName("source").item(0);
        String sourcePath = el.getAttribute("path");
        String sourceType = el.getAttribute("type");
        el = (Element) interactorNode.getElementsByTagName("binary").item(0);
        String binaryPath = "";
        String binaryType = "";
        if (el != null) {
            binaryPath = el.getAttribute("path");
            binaryType = el.getAttribute("type");
//            FileUtils.copyFile(new File(problemDirectory, sourcePath), new File(problemDirectory, "interact.cpp"));
//            if (binaryPath != null) {
//                FileUtils.copyFile(new File(problemDirectory, binaryPath), new File(problemDirectory, "interact.exe"));
//            }
        }

        if (!sourceType.startsWith("cpp")) {
            System.err.println("WARNING: Only C++ interactors are supported, interact.cpp and [interact.exe] are created");
        }
        if (executableProps.getProperty(binaryType) != null) {
            return new Interactor(executableProps.getProperty(binaryType), binaryPath);
        }
        return new Interactor("x86.exe.win32", binaryPath);
    }
}
