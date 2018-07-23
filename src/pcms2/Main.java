package pcms2;

import java.io.*;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        // args description
        // 0 - challenge or problem
        // 1 - challenge id in pcms
        // 2 - challenge type ioi or icpc
        // 3 - path to folder contest.xml or problem.xml, empty if launched in same folder
        if (args.length < 3) {
            System.out.println("usage\n <challenge or problem> <challenge id> <ioi or icpc> [path to contest.xml or problem.xml folder]");
            return;
        }
        String folder = (args.length > 3 ? args[3] : ".");

        try {
            Properties props = load("import.properties");
            String vfs = props.getProperty("vfs", null);
            String webroot = props.getProperty("webroot", null);
            String defaultLanguage = props.getProperty("defaultLanguage");

            Properties languageProps = load("language.properties");
            Properties executableProps = load("executable.properties");

            Boolean update = false;

            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

            if (args[0].equals("problem")) {
                //Problem pi = new Problem("problem.xml", "ru.", "ioi");
                Problem pi = new Problem(folder, args[1], args[2], languageProps, executableProps, defaultLanguage);
                File f = new File(folder, "problem.xml");

                File temporaryFile = new File(folder, "problem.xml.tmp");
                PrintWriter pw = new PrintWriter(new FileWriter(temporaryFile));
                pi.print(pw);
                pw.close();
                (new File(f.getAbsolutePath() + ".old")).delete();
                if (!f.renameTo(new File(f.getAbsolutePath() + ".old"))) {
                    System.out.println("ERROR: '" + f.getAbsolutePath() + "' couldn't be renamed to '.old' ");
                    return;
                }
                if (!temporaryFile.renameTo(new File(temporaryFile.getParent(), "problem.xml"))){
                    System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
                    return;
                }

                if (vfs != null) {
                    pi.copyToVFS(vfs, sysin, update);
                }
            } else if (args[0].equals("challenge")) {
                Challenge ch = new Challenge(args[1], args[2], folder, languageProps, executableProps, defaultLanguage);
                try (PrintWriter pw = new PrintWriter(new FileWriter(new File(folder, "challenge.xml")))) {
                    ch.print(pw);
                    pw.close();
                }
                for (Problem pr : ch.problems.values()) {
                    File temporaryFile = new File(folder, "problems/" + pr.shortName + "/problem.xml.tmp");
                    try (PrintWriter pw = new PrintWriter(new FileWriter(temporaryFile))) {
                        pr.print(pw);
                        pw.close();
                    }
                }

                for (Problem pr : ch.problems.values()) {
                    File temporaryFile = new File(folder, "problems/" + pr.shortName + "/problem.xml.tmp");
                    File f = new File(folder, "problems/" + pr.shortName + "/problem.xml");
                    (new File(f.getAbsolutePath() + ".old")).delete();
                    if (!f.renameTo(new File(f.getAbsolutePath() + ".old"))) {
                        System.out.println("ERROR: '" + f.getAbsolutePath() + "' couldn't be renamed to '.old' ");
                        return;
                    }
                    if (!temporaryFile.renameTo(new File(temporaryFile.getParent(), "problem.xml"))){
                        System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
                        return;
                    }
                    if (vfs != null) {
                        update = pr.copyToVFS(vfs, sysin, update);
                    }
                }
                if (vfs != null) {
                    ch.copyToVFS(vfs, sysin, update);
                }
                if (webroot != null) {
                    ch.copyToWEB(webroot, sysin);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Properties load(String fileName) throws IOException {
        Properties props = new Properties();
        File propsFile;
        if (System.getenv().get("lib_home") != null) {
            propsFile = new File(System.getenv().get("lib_home"), fileName);
        } else {
            propsFile = new File(fileName);
        }
        if (propsFile.exists()) {
            InputStreamReader in = new InputStreamReader(new FileInputStream(propsFile), "UTF-8");
            props.load(in);
            in.close();
        }
        return props;
    }
}
