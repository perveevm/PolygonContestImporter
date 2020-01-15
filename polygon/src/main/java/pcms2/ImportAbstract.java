package pcms2;

import org.xml.sax.SAXException;
import picocli.CommandLine.Option;

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

    @Override
    public Integer call() {
        System.err.println("here");
        try {
            Properties props = load(new Properties(), "import.properties");
            vfs = props.getProperty("vfs", null);
            webroot = props.getProperty("webroot", null);
            defaultLanguage = props.getProperty("defaultLanguage", "english");
            languageProps = load(getDefaultLanguageProperties(), "language.properties");
            executableProps = load(getDefaultExecutableProperties(), "executable.properties");
            sysin = new BufferedReader(new InputStreamReader(System.in));
            makeImport();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    abstract protected void makeImport() throws IOException, ParserConfigurationException, SAXException;

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
