package importer;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import pcms2.Challenge;
import pcms2.Problem;

import java.io.*;

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

    static public void copyToVFS(File srcContestDir, Challenge challenge, File vfs, Asker asker) throws IOException {
        String[] files = {"challenge.xml", "submit.lst"};
        File vfsEtcDirectory = new File(vfs, "etc/" + challenge.getId().replace(".", "/"));
        for (String f : files) {
            File src = new File(srcContestDir, f);
            File dest = new File(vfsEtcDirectory, f);
            System.out.println("Preparing to copy " + f + " to " + dest.getAbsolutePath());
            deployFile(src, dest, asker);
        }
    }

    static public void forceCopyFileOrDirectory(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            FileUtils.copyDirectory(src, dest);
        } else {
            FileUtils.copyFile(src, dest);
        }
    }

    static public void deployFile(File src, File dest, Asker asker) throws IOException {
        if (dest.exists()) {
            System.out.println(src.getName() + " '" + dest.getAbsolutePath() + "' exists.");
            if (asker.askForUpdate("Do you want to update it?")) {
                System.out.println("Updating...");
                forceCopyFileOrDirectory(src, dest);
            } else {
                System.out.println("Skipping...");
            }
        } else {
            System.out.println("Copying " + src.getName() + " to '" + dest.getAbsolutePath() + "'.");
            forceCopyFileOrDirectory(src, dest);
        }
    }

    static public void publishFile(File src, File dest, Asker asker) throws IOException {
        if (asker.askForUpdate("Do you want to publish it?")) {
            System.out.println("Publishing...");
            forceCopyFileOrDirectory(src, dest);
        } else {
            System.out.println("Skipping...");
        }
    }

    static public void copyToWEB(File srcContestDir, Challenge challenge, File webroot, Asker asker) throws IOException {
        File src = new File(srcContestDir, "statements/" + challenge.getLanguage() + "/statements.pdf");
        if (!src.exists()) {
            return;
        }
        File dest = new File(webroot, "statements/" + challenge.getId().replace(".", "/") + "/statements.pdf");
        System.out.println("Preparing to copy " + challenge.getLanguage() + " statement to " + dest.getAbsolutePath());
        publishFile(src, dest, asker);
    }

    static public void copyToVFS(Problem problem, File vfs, Asker asker) throws IOException {
        File src = problem.getDirectory();
        File dest = resolveProblemVfs(vfs, problem.getId());
        System.out.println("Preparing to copy problem " + problem.getShortName() + " to " + dest.getAbsolutePath());
        deployFile(src, dest, asker);
    }

    static public File resolveProblemVfs(File vfs, String problemId) {
        return new File(vfs, "problems/" + problemId.replace(".", "/"));
    }

    static public boolean checkerQuitsPoints(File checker) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(checker));
        String line;
        while ((line = br.readLine()) != null){
            if (line.contains("_points") || line.contains("quitp")) {
                return true;
            }
        }
        return false;
    }
}
