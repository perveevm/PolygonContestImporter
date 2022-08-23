package converter;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.xml.sax.SAXException;
import pcms2.Problem;
import polygon.Checker;
import polygon.ProblemDirectory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Stream;

public class Converter {
    private final static Logger log = LogManager.getLogger(Converter.class);
    private final Properties importProps;
    private final Properties languageProps;
    private final Properties executableProps;

    public Converter(Properties importProps,
                     Properties languageProps,
                     Properties executableProps) {
        this.importProps = importProps;
        this.languageProps = languageProps;
        this.executableProps = executableProps;
    }

    private int runDoAll(File probDir, boolean quiet) throws IOException {
        ProcessBuilder processBuilder = System.getProperty("os.name").toLowerCase().startsWith("win") ?
                new ProcessBuilder("cmd", "/c", "doall.bat") :
                new ProcessBuilder("/bin/bash", "-c", "find -name '*.sh' | xargs chmod +x && ./doall.sh");
        processBuilder.directory(probDir);
        boolean ignored = processBuilder.redirectErrorStream();
        Process exec = processBuilder.start();
        log.info("Starting PID = " + exec.pid());
        if (!quiet) {
            try (OutputStream logStream = IoBuilder.forLogger(log).buildOutputStream()) {
                IOUtils.copy(exec.getInputStream(), logStream);
            }
        }
        try {
            return exec.waitFor();
        } catch (InterruptedException e) {
            log.warn("The process was interrupted");
            return 130;
        }
    }

    public void problemDoAll(Problem problem) throws IOException {
        int exitCode = runDoAll(problem.getDirectory(), false);
        if (exitCode != 0) {
            throw new AssertionError("doall failed with exit code " + exitCode);
        } else {
            log.info("Tests generated successfully in " + problem.getDirectory().getAbsolutePath());
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
        Problem problem = new Problem(problemDirectory, problemIdPrefix, languageProps, executableProps, importProps);
        if (runDoAll) {
            problemDoAll(problem);
        }
        Checker checker = problem.getPolygonProblem().getChecker();
        String checkerSourceName = checker.getSource();
        File probDir = problem.getDirectory();
        File checkerFile = new File(probDir, checkerSourceName);
        RecompileCheckerStrategy recompileCppChecker = RecompileCheckerStrategy.valueOf(importProps.getProperty("recompileChecker", "never").toUpperCase());
        if (recompileCppChecker == RecompileCheckerStrategy.ALWAYS ||
                recompileCppChecker == RecompileCheckerStrategy.POINTS && checkerQuitsPoints(checkerFile)) {
            if (System.getProperty("os.name").toLowerCase().startsWith("win")
                    && checker.getType().equals("testlib") && checker.getSourceType().startsWith("cpp.g++")) {
                String checkerTmpExecutable = "__check.pcms.exe";
                String checkerExecutable = checker.getBinaryPath();
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "g++", "-o", checkerTmpExecutable, checkerSourceName,
                        "-DPCMS2", "-O2", "-std=c++17", "-static", "-Ifiles", "-Wl,--stack=2560000000");
                log.info("Compiling checker " + processBuilder.command());
                processBuilder.directory(probDir);
                processBuilder.inheritIO();
                Process exec = processBuilder.start();
                try {
                    int exitCode = exec.waitFor();
                    if (exitCode != 0) {
                        log.warn("checker compilation failed, exit code " + exitCode);
                    } else {
                        File tmpFile = new File(probDir, checkerTmpExecutable);
                        if (tmpFile.exists() && tmpFile.canRead() && tmpFile.canWrite()) {
                            log.info("Checker compiled successfully");
                            File checkExec = new File(probDir, checkerExecutable);
                            File polygonCheckExec = new File(probDir, checkerExecutable + ".polygon");
                            log.info("moving " + checkExec.getName() + " -> " + polygonCheckExec.getName());
                            if (!checkExec.renameTo(polygonCheckExec)) {
                                log.warn("old checker couldn't be moved");
                            } else {
                                log.info("moving " + tmpFile.getName() + " -> " + checkExec.getName());
                                if (!tmpFile.renameTo(checkExec)) {
                                    log.error("new checker couldn't be moved");
                                    throw new AssertionError("No checker for a problem");
                                }
                            }
                        } else {
                            log.warn("compilation succeeded, but checker binary" +
                                    " doesn't exist or there are no rights");
                        }
                    }
                } catch (InterruptedException e) {
                    log.warn("the compilation was interrupted");
                }
            } else {
                log.warn("checker compilation is supported only in Windows, " +
                        "and only for testlib using g++ sources");
            }
        }
        problem.print(getTemporaryProblemXMLFile(problem));
        File temporaryFile = getTemporaryProblemXMLFile(problem);
        File f = new File(problem.getDirectory(), "problem.xml");
        f.delete();
        if (!temporaryFile.renameTo(f)) {
            log.error("'{}' couldn't be renamed to 'problem.xml' ", temporaryFile.getAbsolutePath());
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
