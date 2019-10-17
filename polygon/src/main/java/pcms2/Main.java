package pcms2;

import polygon.Contest;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class Main {

    static int PROBLEM = 1;
    static int CONTEST = 2;

    public static void main(String[] args) {
        // args description
        // 0 - contest or problem
        // 1 - challenge id in pcms
        // 2 - challenge type ioi or icpc
        // 3 - path to folder contest.xml or problem.xml, empty if launched in same folder
        int type = 0;
        String challengeId = null;
        String challengeType = null;
        String folder = ".";
        boolean updateAll = false;
        int index = -1;
        for (String arg : args) {
            if (arg.startsWith("--")) {
                if (arg.equals("--y")) {
                    updateAll = true;
                } else {
                    usage();
                }
            } else {
                index++;
                switch (index) {
                    case 0:
                        if (arg.equals("contest") || arg.equals("challenge")) {
                            type = CONTEST;
                        } else if (arg.equals("problem")) {
                            type = PROBLEM;
                        } else {
                            usage();
                        }
                        break;
                    case 1:
                        challengeId = arg;
                        break;
                    case 2:
                        if (type == CONTEST) {
                            challengeType = arg;
                        } else if (type == PROBLEM) {
                            folder = arg;
                        }
                        break;
                    case 3:
                        if (type == CONTEST) {
                            folder = arg;
                        } else {
                            usage();
                        }
                        break;
                    default:
                        usage();
                }
            }
        }

        if (type == 0 || challengeId == null || type == CONTEST && challengeType == null) {
            usage();
        }

        try {
            Properties props = load(new Properties(), "import.properties");
            String vfs = props.getProperty("vfs", null);
            String webroot = props.getProperty("webroot", null);
            String defaultLanguage = props.getProperty("defaultLanguage");

            Properties languageProps = load(getDefaultLanguageProperties(), "language.properties");
            Properties executableProps = load(getDefaultExecutableProperties(), "executable.properties");



            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

            if (type == PROBLEM) {
                polygon.Problem polygonProblem = polygon.Problem.parse(folder);
                Problem pi = new Problem(polygonProblem, challengeId, languageProps, executableProps);
                File temporaryFile = new File(folder, "problem.xml.tmp");
                PrintWriter pw = new PrintWriter(new FileWriter(temporaryFile));
                pi.print(pw);
                pw.close();

                File f = new File(folder, "problem.xml");
                f.delete();
                if (!temporaryFile.renameTo(f)) {
                    System.out.println("ERROR: '" + temporaryFile.getAbsolutePath() + "' couldn't be renamed to 'problem.xml' ");
                    return;
                }

                if (vfs != null) {
                    pi.copyToVFS(vfs, sysin, updateAll);
                }
            } else if (type == CONTEST) {
                Contest contest = Contest.parse(folder);
                Challenge ch = new Challenge(contest, challengeId, challengeType, folder, languageProps, executableProps, defaultLanguage);
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
                boolean update = updateAll;
                for (Map.Entry<String, Problem> entry : ch.problems.entrySet()) {
                    //for (Problem pr : ch.problems.values()) {
                    Problem pr = entry.getValue();
                    File f = new File(folder, "problems/" + pr.shortName + "/problem.xml");
                    File temporaryFile = new File(f.getAbsolutePath() + ".tmp");
                    if (f.exists()) {
                        f.delete();
                    }
                    if (!temporaryFile.renameTo(f)) {
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
                    ch.copyToWEB(webroot, sysin, updateAll);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void usage() {
        System.out.println("Usage\n" +
                "    Importing contest: contest <challenge id | auto> <ioi | icpc> [path to contest.xml folder] [--y]\n" +
                "    Importing problem: problem <challenge id | auto> [path to contest.xml folder] [--y]");
        System.exit(1);
    }

    static Properties load(Properties props, String fileName) throws IOException {
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
