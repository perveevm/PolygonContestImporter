package converter;

import org.xml.sax.SAXException;
import pcms2.Problem;
import polygon.Checker;
import polygon.ProblemDirectory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Stream;

public class Converter {
    private final RecompileCheckerStrategy recompileCppChecker;
    private final Properties languageProps;
    private final Properties executableProps;

    public Converter(RecompileCheckerStrategy recompileCppChecker, Properties languageProps, Properties executableProps) {
        this.recompileCppChecker = recompileCppChecker;
        this.languageProps = languageProps;
        this.executableProps = executableProps;
    }

    private static int runDoAll(File probDir, boolean quiet) throws IOException {
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

    public static void problemDoAll(Problem problem) throws IOException {
        int exitCode = runDoAll(problem.getDirectory(), false);
        if (exitCode != 0) {
            throw new AssertionError("doall failed with exit code " + exitCode);
        } else {
            System.out.println("Tests generated successfully in " + problem.getDirectory().getAbsolutePath());
        }
    }

    /**
     * Converts a polygon package to a PCMS problem directory
     * @param problemDir extracted polygon package
     * @param problemIdPrefix PCMS problem-id prefix
     * @param runDoAll whether to run doall.{sh,bat} before converting
     * @return PCMS problem descriptor
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public Problem convertProblem(File problemDir, String problemIdPrefix, boolean runDoAll) throws IOException, ParserConfigurationException, SAXException {
        ProblemDirectory problemDirectory = ProblemDirectory.parse(problemDir.getAbsolutePath());
        Problem problem = new Problem(problemDirectory, problemIdPrefix, languageProps, executableProps);
        if (runDoAll) {
            problemDoAll(problem);
        }
        Checker checker = problem.getPolygonProblem().getChecker();
        String checkerSourceName = checker.getSource();
        File probDir = problem.getDirectory();
        File checkerFile = new File(probDir, checkerSourceName);
        if (recompileCppChecker == RecompileCheckerStrategy.ALWAYS ||
                recompileCppChecker == RecompileCheckerStrategy.POINTS && checkerQuitsPoints(checkerFile)) {
            if (System.getProperty("os.name").toLowerCase().startsWith("win")
                    && checker.getType().equals("testlib") && checker.getSourceType().startsWith("cpp.g++")) {
                String checkerTmpExecutable = "__check.pcms.exe";
                String checkerExecutable = checker.getBinaryPath();
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "g++", "-o", checkerTmpExecutable, checkerSourceName,
                        "-DPCMS2", "-O2", "-std=c++17", "-static", "-Ifiles");
                System.out.println("INFO: Compiling checker " + processBuilder.command());
                processBuilder.directory(probDir);
                processBuilder.inheritIO();
                Process exec = processBuilder.start();
                try {
                    int exitCode = exec.waitFor();
                    if (exitCode != 0) {
                        System.out.println("WARNING: checker compilation failed, exit code " + exitCode);
                    } else {
                        File tmpFile = new File(probDir, checkerTmpExecutable);
                        if (tmpFile.exists() && tmpFile.canRead() && tmpFile.canWrite()) {
                            System.out.println("INFO: Checker compiled successfully");
                            File checkExec = new File(probDir, checkerExecutable);
                            File polygonCheckExec = new File(probDir, checkerExecutable + ".polygon");
                            System.out.println("INFO: moving " + checkExec.getName() + " -> " + polygonCheckExec.getName());
                            if (!checkExec.renameTo(polygonCheckExec)) {
                                System.out.println("WARNING: old checker couldn't be moved");
                            } else {
                                System.out.println("INFO: moving " + tmpFile.getName() + " -> " + checkExec.getName());
                                if (!tmpFile.renameTo(checkExec)) {
                                    System.out.println("ERROR: new checker couldn't be moved");
                                    throw new AssertionError("No checker for a problem");
                                }
                            }
                        } else {
                            System.out.println("WARNING: compilation succeeded, but checker binary" +
                                    " doesn't exist or there are no rights");
                        }
                    }
                } catch (InterruptedException e) {
                    System.err.println("WARNING: the compilation was interrupted");
                }
            } else {
                System.out.println("WARNING: checker compilation is supported only in Windows, " +
                        "and only for testlib using g++ sources");
            }
        }
        problem.print(getTemporaryProblemXMLFile(problem));
        File temporaryFile = getTemporaryProblemXMLFile(problem);
        File f = new File(problem.getDirectory(), "problem.xml");
        f.delete();
        if (!temporaryFile.renameTo(f)) {
            System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
        }
        return problem;
    }

    private static boolean checkerQuitsPoints(File checker) throws IOException {
        try (Stream<String> lines = Files.lines(checker.toPath())) {
            return lines.anyMatch(line -> line.contains("_points") || line.contains("quitp"));
        }
    }

    private static File getTemporaryProblemXMLFile(Problem problem) {
        return new File(problem.getDirectory(), "problem.xml.tmp");
    }
}
