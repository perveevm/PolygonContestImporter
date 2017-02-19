package pcms2;

import java.io.*;
import java.util.Map;
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
            Properties props = new Properties();
            File propsFile = new File("import.properties");
            if (propsFile.exists()) {
                InputStreamReader in = new InputStreamReader(new FileInputStream("import.properties"), "UTF-8");
                props.load(in);
                in.close();
            }
            String vfs = props.getProperty("vfs", null);
            String webroot = props.getProperty("webroot", null);

            Boolean update = false;

            BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));

            if (args[0].equals("problem")) {
                //Problem pi = new Problem("problem.xml", "ru.", "ioi");
                Problem pi = new Problem(folder, args[1], args[2]);
                File f = new File(folder, "problem.xml");
                f.renameTo(new File(f.getAbsolutePath() + ".old"));
                PrintWriter pw = new PrintWriter(new FileWriter(new File(folder, "problem.xml")));
                pi.print(pw);
                pw.close();
                if (vfs != null) {
                    pi.copyToVFS(vfs, sysin, update);
                }
            } else if (args[0].equals("challenge")) {
                Challenge ch = new Challenge(args[1], args[2], folder);
                PrintWriter pw = new PrintWriter(new FileWriter(new File(folder, "challenge.xml")));
                ch.print(pw);
                pw.close();
                for (Problem pr : ch.problems.values()) {
                    File f = new File(folder, "problems/" + pr.shortName + "/problem.xml");
                    f.renameTo(new File(f.getAbsolutePath() + ".old"));
                    pw = new PrintWriter(new FileWriter(new File(folder, "problems/" + pr.shortName + "/problem.xml")));
                    pr.print(pw);
                    pw.close();
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
}
