package pcms2;

import org.xml.sax.SAXException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

@Command(name = "problem", description = "Imports single problem using problem.xml")
public class ImportProblem extends ImportAbstract {
    @Parameters(index = "0", paramLabel = "<prob-id-pref>",
            description = { "Provide problem-id prefix or 'auto' for problem-id generation",
                    "'com.codeforces.polygon.{problem owner}.{problem short name}'"}) String problemIdPrefix;
    @Parameters(index = "1", arity = "0..1", description = "Path to directory, that contains 'problem.xml'", defaultValue = ".") String folder;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        polygon.Problem polygonProblem = polygon.Problem.parse(folder);
        Problem pi = new Problem(polygonProblem, problemIdPrefix, languageProps, executableProps);
        File temporaryFile = new File(folder, "problem.xml.tmp");
        PrintWriter pw = new PrintWriter(new FileWriter(temporaryFile));
        pi.print(pw);
        pw.close();

        File f = new File(folder, "problem.xml");
        f.delete();
        if (!temporaryFile.renameTo(f)) {
            System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
            return;
        }

        if (vfs != null) {
            pi.copyToVFS(vfs, sysin, updateAll);
        }
    }
}
