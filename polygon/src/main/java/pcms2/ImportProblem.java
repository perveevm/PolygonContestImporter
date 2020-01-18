package pcms2;

import net.lingala.zip4j.exception.ZipException;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;

@Command(name = "problem", description = "Imports single problem using problem.xml")
public class ImportProblem extends ImportProblemAbstract {
    @Parameters(index = "1", arity = "0..1", paramLabel = "<package>",
            description = "Path to directory or zip-file, that contains 'problem.xml'", defaultValue = ".") String polygonPackage;
    @Option(names = "--doall", description = "Run doall, before importing") boolean runDoAll;

    @Override
    String prepareProblemDirectory() throws IOException {
        File f = new File(polygonPackage);
        if (!f.isDirectory()) {
            System.out.println(f.getAbsolutePath() + " is not a directory, trying to unzip");
            try {
                File probDir = fileManager.createTemporaryDirectory("__problem");
                Utils.archiveToDirectory(f, probDir, runDoAll);
                return probDir.getAbsolutePath();
            } catch (ZipException e) {
                throw new AssertionError(f.getAbsolutePath() +
                        ": failed to unzip, it is not a directory and probably not a zipfile", e);
            }
        }
        if (runDoAll) {
            int exitCode = Utils.runDoAll(f, false);
            if (exitCode != 0) {
                throw new AssertionError("doall failed with exit code " + exitCode);
            }
        }
        return f.getAbsolutePath();
    }
}
