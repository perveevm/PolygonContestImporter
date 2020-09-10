package importer;

import net.lingala.zip4j.exception.ZipException;
import org.xml.sax.SAXException;
import pcms2.Problem;
import picocli.CommandLine.Option;
import polygon.Checker;
import polygon.ProblemDirectory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

abstract class ImportAbstract implements Callable<Integer> {

    @Option(names = "--y", description = "Yes to all (non interactive)")
    boolean updateAll;
    @Option(names = {"-u", "--user"}, description = "Polygon username")
    String username;
    @Option(names = {"-p", "--password"}, description = "Polygon password", interactive = true)
    String password;

    File vfs;
    Properties importProps;
    Properties languageProps;
    Properties executableProps;
    String defaultLanguage;
    File webroot;
    Scanner sysin = new Scanner(System.in);
    PackageDownloader downloader;
    TemporaryFileManager fileManager = new TemporaryFileManager();
    Asker asker;
    RecompileCheckerStrategy recompileCppChecker;

    static File readFileFromProperties(Properties props, String key) {
        String pathName = props.getProperty(key, null);
        return pathName == null ? null : new File(pathName);
    }

    @Override
    public Integer call() {
        asker = new ScannerPrinterAsker(sysin, System.out, false, updateAll);
        try {
            importProps = load(new Properties(), "import.properties");
            vfs = readFileFromProperties(importProps, "vfs");
            webroot = readFileFromProperties(importProps, "webroot");
            if (username == null) {
                username = importProps.getProperty("polygonUsername", null);
            }
            if (password == null) {
                password = importProps.getProperty("polygonPassword", null);
            }
            defaultLanguage = importProps.getProperty("defaultLanguage", "english");
            languageProps = load(getDefaultLanguageProperties(), "language.properties");
            executableProps = load(getDefaultExecutableProperties(), "executable.properties");
            downloader = new PackageDownloader(username, password);
            recompileCppChecker = RecompileCheckerStrategy.valueOf(importProps.getProperty("recompileChecker", "never").toUpperCase());
            makeImport();
            return 0;
        } catch (Throwable e) {
            e.printStackTrace();
            return 1;
        } finally {
            File[] toRemove = fileManager.filesToRemove();
            if (toRemove.length > 0) {
                String list = Arrays.stream(toRemove).map(x -> " - " + x).collect(Collectors.joining("\n"));
                if (asker.askForUpdate("Remove all created temporary files and directories?\n" + list)) {
                    fileManager.removeAll();
                } else {
                    System.out.println("Skipping...");
                }
            }
        }
    }

    abstract protected void makeImport() throws IOException, ParserConfigurationException, SAXException;

    /**
     * Takes extracted polygon package directory and converts it to PCMS2 problem directory.
     * Copies problem to VFS if needed.
     *
     * @param problemIdPrefix prefix to construct PCMS2 problem-id
     * @param folder          polygon package directory to process
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    protected void importProblem(String problemIdPrefix, String folder, Asker asker) throws IOException, ParserConfigurationException, SAXException {
        Problem pi = new Problem(ProblemDirectory.parse(folder), problemIdPrefix, languageProps, executableProps, importProps);
        processProblem(pi);
        finalizeImportingProblem(pi, asker);
    }

    /**
     * Generates temporary problem.xml from given {@link pcms2.Problem} object
     *
     * @param problem the problem to process
     * @throws IOException
     */
    protected void processProblem(Problem problem) throws IOException {
        Checker checker = problem.getPolygonProblem().getChecker();
        String checkerSourceName = checker.getSource();
        File probDir = problem.getDirectory();
        File checkerFile = new File(probDir, checkerSourceName);

        if (recompileCppChecker == RecompileCheckerStrategy.ALWAYS ||
                recompileCppChecker == RecompileCheckerStrategy.POINTS && Utils.checkerQuitsPoints(checkerFile)) {
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
    }

    /**
     * Moves temporary problem.xml files to primary problem.xml
     * Copies problem to PCMS2 VFS
     *
     * @param problem the problem to process
     * @param asker   an object that handles user interaction to confirm actions
     * @return true, if update all was requested by user
     * @throws IOException
     */
    protected void finalizeImportingProblem(Problem problem, Asker asker) throws IOException {
        File temporaryFile = getTemporaryProblemXMLFile(problem);
        File f = new File(problem.getDirectory(), "problem.xml");
        f.delete();
        if (!temporaryFile.renameTo(f)) {
            System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
        }
        if (vfs != null) {
            Utils.copyToVFS(problem, vfs, asker);
        }
    }

    private static File getTemporaryProblemXMLFile(Problem problem) {
        return new File(problem.getDirectory(), "problem.xml.tmp");
    }

    private static Properties load(Properties props, String fileName) throws IOException {
        File propsFile;
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf("/") + 1) + fileName;
        propsFile = new File(path);
        if (!propsFile.exists()) {
            propsFile = new File(fileName);
        }
        if (propsFile.exists()) {
            InputStreamReader in = new InputStreamReader(new FileInputStream(propsFile), "UTF-8");
            props.load(in);
            in.close();
        }
        return props;
    }

    private static Properties getDefaultLanguageProperties() {
        Properties p = new Properties();
        p.put("h", "cpp.gnu");
        p.put("cpp", "cpp.gnu");
        p.put("c", "c.gnu");
        p.put("pas", "pascal.free");
        p.put("java", "java");
        return p;
    }

    private static Properties getDefaultExecutableProperties() {
        Properties p = new Properties();
        p.put("exe.win32", "x86.exe.win32");
        p.put("jar7", "java.check");
        p.put("jar8", "java.check");
        return p;
    }

    protected String downloadProblemDirectory(String polygonUrl, File probDir) throws IOException {
        File zipFile = fileManager.createTemporaryFile("__archive", ".zip");
        boolean fullPackage = true;
        if (username == null || password == null) {
            throw new AssertionError("Polygon username or password is not set");
        }
        if (!downloader.downloadPackage(polygonUrl, "windows", zipFile)) {
            fullPackage = false;
            if (!downloader.downloadPackage(polygonUrl, null, zipFile)) {
                throw new AssertionError("Couldn't download any package");
            }
        }
        try {
            Utils.archiveToDirectory(zipFile, probDir, !fullPackage);
            fileManager.remove(zipFile);
            return probDir.getAbsolutePath();
        } catch (ZipException e) {
            throw new AssertionError(e);
        }
    }
}
