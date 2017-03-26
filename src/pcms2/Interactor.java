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
    public final String file;

    public Interactor(String type, String file) {
        if (type.startsWith("java")) {
            type = "java"; // TODO
        } else {
            type = "x86.exe.win32";
        }
        this.executableId = type;
        this.file = file;
    }

    public void print(PrintWriter writer, String tabs) throws IOException {
        writer.println(tabs + "<interactor type=\"%testlib\">");
        FileUtils.copyFile(new File(file), new File("interact.cpp"));
        writer.println(tabs + "\t<binary executable-id =\"" + executableId + "\" file =\"interact.exe\" />");
        writer.println(tabs + "</interactor>");
    }
}
