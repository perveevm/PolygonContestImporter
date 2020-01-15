package pcms2;

import org.apache.commons.io.FileUtils;
import polygon.ContestDescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.commons.text.StringEscapeUtils;
import polygon.ProblemDescriptor;

/**
 * Created by Ilshat on 11/24/2015.
 */
public class Challenge {
    String path;
    String name;
    String id;
    String type;
    String language;
    //problem index -> problem
    TreeMap<String, Problem> problems;
    //problem index -> problem name
    TreeMap<String, String> problemNames;

    public Challenge() {
        problems = new TreeMap<>();
        problemNames = new TreeMap<>();
        path = "";
    }

    public Challenge(ContestDescriptor contest, NavigableMap<String, ProblemDescriptor> contestProblems,
                     String ID, String Type, String Path, Properties languageProps, Properties executableProps, String defaultLang) {
        problems = new TreeMap<>();
        problemNames = new TreeMap<>();
        path = Path;
        id = ID;
        type = Type;
        language = defaultLang;
        parse(contest, contestProblems, languageProps, executableProps, defaultLang);
    }

    void parse(ContestDescriptor contest, NavigableMap<String, ProblemDescriptor> contestProblems, Properties languageProps, Properties executableProps, String defaultLang) {

        String url = contest.getUrl();
        if (id.equals("auto")) {
            String[] t = url.split("/");
            id = "com.codeforces.polygon." + t[t.length - 1];
        }

        //names
        if (contest.getContestNames().containsKey(defaultLang)) {
            name = contest.getContestNames().get(defaultLang);
        } else {
            name = contest.getContestNames().firstEntry().getValue();
            name = StringEscapeUtils.escapeXml11(name);
            System.out.println("WARNING: Challenge name for default language '" + defaultLang + "' not found! Using '" + contest.getContestNames().firstKey() + "' name.");
        }

        for (Map.Entry<String, ProblemDescriptor> entry : contestProblems.entrySet()) {
            String index = entry.getKey();
            Problem p = new Problem(entry.getValue(), id, languageProps, executableProps);
            problems.put(index, p);
            String problemName = entry.getValue().getNames().getOrDefault(defaultLang,
                    entry.getValue().getNames().get(contest.getContestNames().firstKey()));
            problemName = StringEscapeUtils.escapeXml11(problemName);
            problemNames.put(index, problemName);
        }
    }

    public void print(PrintWriter pw) {
        pw.println("<?xml version = \"1.0\" encoding = \"utf-8\" ?>");
        pw.println("<challenge");
        pw.printf("\tid = \"%s\"\n", id);
        pw.printf("\tname = \"%s\"\n", name);
        pw.printf("\tscoring-model = \"%%%s\"\n", type);
        pw.println("\tscoring-mode = \"group-max\"");
        pw.println("\txmlai-process = \"http://neerc.ifmo.ru/develop/pcms2/xmlai/default-rules.xml\"");
        pw.println(">");
        for (Map.Entry<String, Problem> e : problems.entrySet()) {
            pw.printf("\t<problem-ref id = \"%s\" problem-id = \"%s\" name = \"%s\"/>\n",
                    e.getKey().toUpperCase(), e.getValue().id, problemNames.get(e.getKey()));
        }
        pw.println("</challenge>");

    }

    public boolean copyToVFS(String vfs, BufferedReader in, boolean update) throws IOException {
        String[] files = {"challenge.xml", "submit.lst"};
        for (String f : files) {
            copyFileToVFS(vfs, in, update, new File(path, f));
        }
        return update;
    }

    public void copyFileToVFS(String vfs, BufferedReader in, boolean update, File src) throws IOException {
        String fileName = src.getName();
        File dest = new File(vfs + "/etc/" + id.replace(".", "/") + "/" + fileName);
        //System.out.println("DEBUG: src = '" + src.getAbsolutePath() + " dest = '" + dest.getAbsolutePath() + "'");
        if (dest.exists()) {
            System.out.println(fileName + " '" + dest.getAbsolutePath() + "' exists.");
            String yn = "n";
            if (!update) {
                System.out.println("Do You want to update it?\n(y - yes, n - no)");
                yn = in.readLine();
            }
            if (update || yn.equals("y")) {
                System.out.println("Updating...");
                FileUtils.copyFileToDirectory(src, dest.getParentFile());
            } else {
                System.out.println("Skipping...");
            }
        } else {
            System.out.println("Copying " + fileName + " to '" + dest.getAbsolutePath() + "'.");
            FileUtils.copyFileToDirectory(src, dest.getParentFile());
        }
    }

    public void copyToWEB(String webroot, BufferedReader in, boolean update) throws IOException {
        File src = new File(path, "statements/" + language + "/statements.pdf");
        copyStatement(webroot, in, update, src);
    }

    public void copyStatement(String webroot, BufferedReader in, boolean update, File src) throws IOException {
        File dest = new File(webroot + "/statements/" + id.replace(".", "/") + "/statements.pdf");
        //System.out.println("DEBUG: src = '" + src.getAbsolutePath() + " dest = '" + dest.getAbsolutePath() + "'");
        if (src.exists()) {
            System.out.println("Statements '" + src.getAbsolutePath() + "' exists.");
            String yn = "n";
            if (!update) {
                System.out.println("Do You want to publish it?\n(y - yes, n - no)");
                yn = in.readLine();
            }
            if (update || yn.equals("y")) {
                System.out.println("Publishing...");
                FileUtils.copyFileToDirectory(src, dest.getParentFile());
            } else {
                System.out.println("Skipping...");
            }
        }
    }
}
