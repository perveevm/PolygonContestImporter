package pcms2;

import org.xml.sax.SAXException;
import picocli.CommandLine.Option;
import polygon.ProblemDescriptor;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.Callable;

abstract class ImportAbstract implements Callable<Integer> {

    @Option(names = "--y", description = "Update all files") boolean updateAll;
    String vfs;
    Properties languageProps;
    Properties executableProps;
    String defaultLanguage;
    String webroot;
    BufferedReader sysin;
    String login;
    String password;
    PackageDownloader downloader;

    @Override
    public Integer call() {
        try {
            Properties props = load(new Properties(), "import.properties");
            vfs = props.getProperty("vfs", null);
            webroot = props.getProperty("webroot", null);
            login = props.getProperty("polygonLogin", null);
            password = props.getProperty("polygonPassword", null);
            defaultLanguage = props.getProperty("defaultLanguage", "english");
            languageProps = load(getDefaultLanguageProperties(), "language.properties");
            executableProps = load(getDefaultExecutableProperties(), "executable.properties");
            sysin = new BufferedReader(new InputStreamReader(System.in));
            downloader = new PackageDownloader(login, password);
            makeImport();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    abstract protected void makeImport() throws IOException, ParserConfigurationException, SAXException;

    /**
     * Takes extracted polygon package directory and converts it to PCMS2 problem directory.
     * Copies problem to VFS if needed.
     * @param problemIdPrefix prefix to construct PCMS2 problem-id
     * @param folder polygon package directory to process
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    protected void importProblem(String problemIdPrefix, String folder) throws IOException, ParserConfigurationException, SAXException {
        Problem pi = new Problem(ProblemDescriptor.parse(folder), problemIdPrefix, languageProps, executableProps);
        generateTemporaryProblemXML(pi);
        finalizeImportingProblem(pi, updateAll);
    }

    /**
     * Generates temporary problem.xml from given {@link pcms2.Problem} object
     * @param problem the problem to process
     * @throws IOException
     */
    protected void generateTemporaryProblemXML(Problem problem) throws IOException {
        File temporaryFile = getTemporaryProblemXMLFile(problem);
        try (PrintWriter pw = new PrintWriter(new FileWriter(temporaryFile))) {
            problem.print(pw);
        }
    }

    /**
     * Moves temporary problem.xml files to primary problem.xml
     * Copies problem to PCMS2 VFS
     * @param problem the problem to process
     * @param update whether to copy to VFS without asking
     * @return true, if update all was requested by user
     * @throws IOException
     */
    protected boolean finalizeImportingProblem(Problem problem, boolean update) throws IOException {
        File temporaryFile = getTemporaryProblemXMLFile(problem);
        File f = new File(problem.getDirectory(), "problem.xml");
        f.delete();
        if (!temporaryFile.renameTo(f)) {
            System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
        }
        if (vfs != null) {
            update = problem.copyToVFS(vfs, sysin, update);
        }
        return update;
    }

    private File getTemporaryProblemXMLFile(Problem problem) {
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
}
