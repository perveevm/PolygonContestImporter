package pcms2;

import polygon.Contest;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        // args description
        // 0 - contest or problem
        // 1 - challenge id in pcms
        // 2 - challenge type ioi or icpc
        // 3 - path to folder contest.xml or problem.xml, empty if launched in same folder
        if (args.length < 2 || args.length < 3 && !args[0].equals("problem")) {
            System.out.println("usage\n <contest or problem> <challenge id> <ioi or icpc> [path to contest.xml or problem.xml folder]");
            return;
        }
        String folder = (args.length > 3 ? args[3] : ".");

        try {
            Properties props = load(new Properties(), "import.properties");
            String vfs = props.getProperty("vfs", null);
            String webroot = props.getProperty("webroot", null);
            String defaultLanguage = props.getProperty("defaultLanguage");

            Properties languageProps = load(getDefaultLanguageProperties(), "language.properties");
            Properties executableProps = load(getDefaultExecutableProperties(), "executable.properties");

            Boolean update = false;

            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

            if (args[0].equals("problem")) {
                polygon.Problem polygonProblem = polygon.Problem.parse(folder);
                Problem pi = new Problem(polygonProblem, args[1], languageProps, executableProps);
                File temporaryFile = new File(folder, "problem.xml.tmp");
                PrintWriter pw = new PrintWriter(new FileWriter(temporaryFile));
                pi.print(pw);
                pw.close();

                File f = new File(folder, "problem.xml");
                f.delete();
                if (!temporaryFile.renameTo(f)){
                    System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
                    return;
                }

                if (vfs != null) {
                    pi.copyToVFS(vfs, sysin, update);
                }
            } else if (args[0].equals("challenge") || args[0].equals("contest")) {
                Contest contest = Contest.parse(folder);
                Challenge ch = new Challenge(contest, args[1], args[2], folder, languageProps, executableProps, defaultLanguage);
                try (PrintWriter pw = new PrintWriter(new FileWriter(new File(folder, "challenge.xml")))) {
                    ch.print(pw);
                }

                for (Problem pr : ch.problems.values()) {
                    File temporaryFile = new File(folder, "problems/" + pr.shortName + "/problem.xml.tmp");
                    try (PrintWriter pw = new PrintWriter(new FileWriter(temporaryFile))) {
                        pr.print(pw);
                    }
                }
                File submitFile = new File(folder, "submit.lst");
                PrintWriter submit = new PrintWriter(new FileWriter(submitFile));
                for (Map.Entry<String, Problem> entry : ch.problems.entrySet()){
                //for (Problem pr : ch.problems.values()) {
                    Problem pr = entry.getValue();
                    File f = new File(folder, "problems/" + pr.shortName + "/problem.xml");
                    File temporaryFile = new File(f.getAbsolutePath() + ".tmp");
                    if (f.exists()) {
                        f.delete();
                    }
                    if (!temporaryFile.renameTo(f)){
                        System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
                        return;
                    }
                    if (vfs != null) {
                        update = pr.copyToVFS(vfs, sysin, update);
                        pr.printSolutions(submit, ch.id + ".0", entry.getKey().toUpperCase(), languageProps, vfs);
                    }
                }
                submit.close();

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

    static Properties load(Properties props, String fileName) throws IOException {
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

    static Properties getDefaultLanguageProperties() {
        Properties p = new Properties();
        p.put("h", "cpp.gnu");
        p.put("cpp", "cpp.gnu");
        p.put("c", "c.gnu");
        p.put("pas", "pascal.free");
        p.put("java", "java");
        return p;
    }

    static Properties getDefaultExecutableProperties() {
        Properties p = new Properties();
        p.put("exe.win32", "x86.exe.win32");
        p.put("jar7", "java.check");
        p.put("jar8", "java.check");
        return p;
    }
}
