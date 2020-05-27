package importer;

import converter.Converter;
import converter.RecompileCheckerStrategy;
import importer.properties.PolygonPackageType;
import net.lingala.zip4j.exception.ZipException;
import org.xml.sax.SAXException;
import pcms2.Problem;
import picocli.CommandLine.Option;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class ImportAbstract implements Callable<Integer> {

    @Option(names = "--y", description = "Yes to all (non interactive)")
    boolean updateAll;
    @Option(names = {"-u", "--user"}, description = "Polygon username")
    String username;
    @Option(names = {"-p", "--password"}, description = "Polygon password", interactive = true)
    String password;
    @Option(names = {"--import-props"}, description = "import.properties file path")
    String importPropsPath;

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
    Converter converter;

    static File readFileFromProperties(Properties props, String key) {
        String pathName = props.getProperty(key, null);
        return pathName == null ? null : new File(pathName);
    }

    @Override
    public Integer call() {
        asker = new ScannerPrinterAsker(sysin, System.out, false, updateAll);
        try {
            importProps = loadPropertiesOrDefault(new Properties(), "import.properties", importPropsPath);
            vfs = readFileFromProperties(importProps, "vfs");
            webroot = readFileFromProperties(importProps, "webroot");
            if (username == null) {
                username = importProps.getProperty("polygonUsername", null);
            }
            if (password == null) {
                password = importProps.getProperty("polygonPassword", null);
            }
            defaultLanguage = importProps.getProperty("defaultLanguage", "english");
            languageProps = loadPropertiesOrDefault(getDefaultLanguageProperties(), "language.properties");
            executableProps = loadPropertiesOrDefault(getDefaultExecutableProperties(), "executable.properties");
            downloader = new PackageDownloader(username, password);
            converter = new Converter(RecompileCheckerStrategy.valueOf(importProps.getProperty("recompileChecker", "never").toUpperCase()),
                                      languageProps, executableProps);
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
     * Copies problem to PCMS2 VFS
     *
     * @param problem the problem to process
     * @param asker   an object that handles user interaction to confirm actions
     * @return true, if update all was requested by user
     * @throws IOException
     */
    protected void copyProblemToVfs(Problem problem, Asker asker) throws IOException {
        if (vfs != null) {
            Utils.copyToVFS(problem, vfs, asker);
        }
    }

    private static Properties loadPropertiesOrDefault(Properties defaultProps, String fileName, String... filesToTry) {
        Stream<File> defaultFiles = Stream.of(getPropertiesDefault(fileName), new File(fileName));
        Stream<File> filesToTryStream = Arrays.stream(filesToTry).filter(Objects::nonNull).map(File::new);
        return Stream.concat(filesToTryStream, defaultFiles).map(file -> {
            try {
                return loadFile(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).filter(Objects::nonNull).findFirst().orElse(defaultProps);
    }

    private static Properties loadFile(File file) throws IOException {
        return !file.exists() ? null : loadPropertiesFromFile(file);
    }

    private static File getPropertiesDefaultDirectory() {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    private static File getPropertiesDefault(String fileName) {
        return new File(getPropertiesDefaultDirectory(), fileName);
    }

    private static Properties loadPropertiesFromFile(File propsFile) throws IOException {
        Properties props = new Properties();
        if (propsFile.exists()) {
            try (InputStreamReader in = new InputStreamReader(new FileInputStream(propsFile), "UTF-8")) {
                props.load(in);
            }
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

    protected PolygonPackageType downloadProblemDirectory(String polygonUrl, File probDir) throws IOException {
        try {
            File zipFile = fileManager.createTemporaryFile("__archive", ".zip");
            PolygonPackageType fullPackage = downloadProblemPackage(polygonUrl, zipFile);
            Utils.unzip(zipFile, probDir);
            fileManager.remove(zipFile);
            return fullPackage;
        } catch (ZipException e) {
            throw new AssertionError(e);
        }
    }

    private PolygonPackageType downloadProblemPackage(String polygonUrl, File zipFile) throws IOException {
        if (username == null || password == null) {
            throw new AssertionError("Polygon username or password is not set");
        }
        if (downloader.downloadPackage(polygonUrl, "windows", zipFile)) {
            return PolygonPackageType.WINDOWS;
        }
        if (downloader.downloadPackage(polygonUrl, null, zipFile)) {
            return PolygonPackageType.STANDARD;
        }
        throw new AssertionError("Couldn't download any package");
    }
}
