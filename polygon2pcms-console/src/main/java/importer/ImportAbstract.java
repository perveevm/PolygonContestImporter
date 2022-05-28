package importer;

import converter.Converter;
import converter.RecompileCheckerStrategy;
import pcms2.deployer.Deployer;
import org.xml.sax.SAXException;
import pcms2.deployer.DeployerConfig;
import pcms2.Problem;
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
            if (defaultLanguage == null) {
                defaultLanguage = importProps.getProperty("defaultLanguage", "english");
            }
            languageProps = loadPropertiesOrDefault(getDefaultLanguageProperties(), "language.properties");
            executableProps = loadPropertiesOrDefault(getDefaultExecutableProperties(), "executable.properties");
            downloader = new PackageDownloader(username, password);
            converter = new Converter(importProps, languageProps, executableProps, System.out);
            deployer = new Deployer(vfs, webroot, System.out);
            fileManager = new TemporaryFileManager(tempDir);
            makeImport();
            return 0;
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            return 13;
        } catch (Throwable e) {
            e.printStackTrace();
            return 1;
        } finally {
            File[] toRemove = fileManager.filesToRemove();
            if (toRemove.length > 0) {
                if (!keepTemporary) {
                    fileManager.removeAll();
                } else {
                    String list = Arrays.stream(toRemove).map(x -> " - " + x).collect(Collectors.joining("\n"));
                    System.out.println("These temporary downloaded or created files weren't removed:\n" + list);
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
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
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
}
