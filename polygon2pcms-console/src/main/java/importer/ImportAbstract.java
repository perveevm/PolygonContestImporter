package importer;

import converter.Converter;
import net.lingala.zip4j.exception.ZipException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pcms2.deployer.Deployer;
import org.xml.sax.SAXException;
import pcms2.deployer.DeployerConfig;
import pcms2.Problem;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Option;
import polygon.download.PackageDownloader;
import tempfilemanager.TemporaryFileManager;

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

    private final static Logger logger = LogManager.getLogger(ImportAbstract.class);

    @Spec CommandSpec spec;
    @Option(names = "--y", description = "Yes to all (non interactive)")
    boolean updateAll;
    @Option(names = {"-u", "--user"}, description = "Polygon username")
    String username;
    @Option(names = {"-p", "--password"}, description = "Polygon password", interactive = true)
    String password;
    @Option(names = {"--import-props"}, description = "import.properties file path")
    String importPropsPath;
    @Option(names = {"--language"}, description = "Language for problem names and problem statements")
    String defaultLanguage;
    @Option(names = {"--temp-dir"}, description = "Directory for temporary downloaded and created files")
    File tempDir;
    @Option(names = {"--keep-temp"}, description = "Don't remove temporary downloaded and created files")
    boolean keepTemporary;
    @Option(names = {"--admin-login"}, description = "Admin login for auto-submit script. It shouldn't be the full session or party name, only login.")
    String adminLogin;
    @Option(names = {"--add-suffix"}, description = "If language names (for example cpp.gnu) and executable names (for example x86.exe.win32) should be finished with some contest-specific suffix")
    String langSuffix;
    @Option(names = {"--auto-submit-dir"}, description = "The path where importer have to copy submit.lst file (the file will also be copied to VFS)")
    File autoSubmitDir;

    File vfs;
    Properties importProps;
    Properties languageProps;
    Properties executableProps;
    File webroot;
    Scanner sysin = new Scanner(System.in);
    PackageDownloader downloader;
    TemporaryFileManager fileManager;
    Asker asker;
    Converter converter;
    Deployer deployer;

    static File readFileFromProperties(Properties props, String key) {
        String pathName = props.getProperty(key, null);
        return pathName == null ? null : new File(pathName);
    }

    @Override
    public Integer call() {
        PrintWriter stdout = spec.commandLine().getOut();
        asker = new ScannerPrinterAsker(sysin, stdout, false, updateAll);
        try {
            importProps = loadPropertiesOrDefault(new Properties(), null, "import.properties", importPropsPath);
            vfs = readFileFromProperties(importProps, "vfs");
            webroot = readFileFromProperties(importProps, "webroot");
            if (username == null) {
                username = importProps.getProperty("polygonUsername", null);
            }
            if (password == null) {
                password = importProps.getProperty("polygonPassword", null);
            }
            if (defaultLanguage == null) {
                defaultLanguage = importProps.getProperty("defaultLanguage", "english");
            }
            if (adminLogin == null) {
                adminLogin = importProps.getProperty("adminLogin", "0");
            }
            if (langSuffix == null) {
                langSuffix = importProps.getProperty("addSuffix", null);
            }
            if (autoSubmitDir == null) {
                autoSubmitDir = readFileFromProperties(importProps, "autoSubmitDir");
            }
            languageProps = loadPropertiesOrDefault(getDefaultLanguageProperties(), langSuffix, "language.properties");
            executableProps = loadPropertiesOrDefault(getDefaultExecutableProperties(), langSuffix, "executable.properties");
            downloader = new PackageDownloader(username, password);
            converter = new Converter(importProps, languageProps, executableProps);
            deployer = new Deployer(vfs, webroot);
            fileManager = new TemporaryFileManager(tempDir);
            makeImport();
            return 0;
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            return 13;
        } catch (Throwable e) {
            e.printStackTrace();
            return 1;
        } finally {
            File[] toRemove = fileManager.filesToRemove();
            if (toRemove.length > 0) {
                if (!keepTemporary) {
                    try {
                        fileManager.removeAll();
                    } catch (IOException e) {
                        logger.warn("Some files couldn't be removed: {}", e.getMessage());
                        // ignore
                    }
                } else {
                    String list = Arrays.stream(toRemove).map(x -> " - " + x).collect(Collectors.joining("\n"));
                    logger.info("These temporary downloaded or created files weren't removed:\n{}", list);
                }
            }
        }
    }

    abstract protected void makeImport() throws IOException, ParserConfigurationException, SAXException;

    /**
     * Copies problem to PCMS2 VFS
     *
     * @param problem the problem to process
     * @param config   an object that handles user interaction to confirm actions
     * @return true, if update all was requested by user
     * @throws IOException
     */
    protected void copyProblemToVfs(Problem problem, DeployerConfig config) throws IOException {
        if (vfs != null) {
            deployer.copyToVFS(problem, config);
        }
    }

    protected File acquireDirectory(File f, TemporaryFileManager fileManager) throws IOException {
        if (!f.isDirectory()) {
            logger.info(f.getAbsolutePath() + " is not a directory, trying to unzip");
            try {
                File dir = fileManager.createTemporaryDirectory("__tmpdir");
                Utils.unzip(f, dir);
                return dir;
            } catch (ZipException e) {
                throw new AssertionError(f.getAbsolutePath() +
                        ": failed to unzip, it is not a directory and probably not a zipfile", e);
            }
        }
        return f;
    }

    private static Properties loadPropertiesOrDefault(Properties defaultProps, String suffix, String fileName, String... filesToTry) {
        Stream<File> defaultFiles = Stream.of(getPropertiesDefault(fileName), new File(fileName));
        Stream<File> filesToTryStream = Arrays.stream(filesToTry).filter(Objects::nonNull).map(File::new);
        return Stream.concat(filesToTryStream, defaultFiles).map(file -> {
            try {
                return loadFile(file, suffix);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).filter(Objects::nonNull).findFirst().orElse(defaultProps);
    }

    private static Properties loadFile(File file, String suffix) throws IOException {
        return !file.exists() ? null : loadPropertiesFromFile(file, suffix);
    }

    private static File getPropertiesDefaultDirectory() {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
    }

    private static File getPropertiesDefault(String fileName) {
        return new File(getPropertiesDefaultDirectory(), fileName);
    }

    private static Properties loadPropertiesFromFile(File propsFile, String suffix) throws IOException {
        Properties props = new Properties();
        if (propsFile.exists()) {
            try (InputStreamReader in = new InputStreamReader(new FileInputStream(propsFile), "UTF-8")) {
                props.load(in);
            }
        }
        if (suffix != null) {
            for (Object key : props.keySet()) {
                String value = props.getProperty((String) key);
                props.replace(key, value + suffix);
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
}
