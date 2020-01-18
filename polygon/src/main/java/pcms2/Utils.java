package pcms2;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;

public class Utils {
    public static int runDoAll(File probDir, boolean quiet) throws IOException {
        ProcessBuilder processBuilder = System.getProperty("os.name").toLowerCase().startsWith("win") ?
                new ProcessBuilder("cmd", "/c", "doall.bat") :
                new ProcessBuilder("/bin/bash", "-c", "find -name '*.sh' | xargs chmod +x && ./doall.sh");
        processBuilder.directory(probDir);
        if (!quiet) {
            processBuilder.inheritIO();
        }
        Process exec = processBuilder.start();
        try {
            return exec.waitFor();
        } catch (InterruptedException e) {
            System.err.println("The process was interrupted");
            return 130;
        }
    }

    public static void archiveToDirectory(File zipFile, File probDir, boolean runDoAll) throws IOException, ZipException {

        System.out.println("Unzipping " + zipFile.getAbsolutePath());
        unzip(zipFile, probDir);

        System.out.println("Problem downloaded to " + probDir.getAbsolutePath());
        if (runDoAll) {
            System.out.println("Standard package, initiating test generation");
            int exitCode = Utils.runDoAll(probDir, false);
            if (exitCode != 0) {
                throw new AssertionError("doall failed with exit code " + exitCode);
            } else {
                System.out.println("Tests generated successfully in " + probDir.getAbsolutePath());
            }
        }
    }

    static public void unzip(File zipFile, File probDir) throws ZipException {
        new ZipFile(zipFile).extractAll(probDir.getAbsolutePath());
    }
}
