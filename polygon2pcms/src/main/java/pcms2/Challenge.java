package pcms2;

import polygon.ContestDescriptor;

import java.io.*;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.apache.commons.text.StringEscapeUtils;
import polygon.ProblemDescriptor;

/**
 * Created by Ilshat on 11/24/2015.
 */
public class Challenge {
    String name;
    String id;
    String type;
    String language;
    //problem index -> problem id
    NavigableMap<String, String> problemIds;
    //problem index -> problem name
    NavigableMap<String, String> problemNames;


    public Challenge(ContestDescriptor contest, String ID, String Type, String defaultLang) {
        problemIds = new TreeMap<>();
        problemNames = new TreeMap<>();
        id = ID;
        type = Type;
        language = defaultLang;
        parse(contest, defaultLang);
    }

    void parse(ContestDescriptor contest, String defaultLang) {

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
            System.out.println("WARNING: Challenge name for default language '" + defaultLang + "' not found! Using '" + contest.getContestNames().firstKey() + "' name.");
        }

        for (Map.Entry<String, ProblemDescriptor> entry : contest.getProblems().entrySet()) {
            String index = entry.getKey();
            ProblemDescriptor probDesc = entry.getValue();
            problemIds.put(index, Problem.getProblemId(id, probDesc.getUrl(), probDesc.getShortName()));
            String problemName = probDesc.getNames().getOrDefault(defaultLang,
                    probDesc.getNames().get(contest.getContestNames().firstKey()));
            problemNames.put(index, problemName);
        }
    }

    public void print(File file) throws FileNotFoundException, UnsupportedEncodingException {
        String encoding = "utf-8";
        try (PrintWriter pw = new PrintWriter(file, encoding)) {
            pw.println(String.format("<?xml version = \"1.0\" encoding = \"%s\" ?>", encoding));
            print(pw);
        }
    }

    public void print(PrintWriter pw) {
        pw.println("<challenge");
        pw.printf("\tid = \"%s\"\n", id);
        pw.printf("\tname = \"%s\"\n", StringEscapeUtils.escapeXml11(name));
        pw.printf("\tscoring-model = \"%%%s\"\n", type);
        pw.println("\tscoring-mode = \"group-max\"");
        pw.println("\txmlai-process = \"http://neerc.ifmo.ru/develop/pcms2/xmlai/default-rules.xml\"");
        pw.println(">");
        for (Map.Entry<String, String> e : problemIds.entrySet()) {
            pw.printf("\t<problem-ref id = \"%s\" problem-id = \"%s\" name = \"%s\"/>\n",
                    e.getKey().toUpperCase(), e.getValue(), StringEscapeUtils.escapeXml11(problemNames.get(e.getKey())));
        }
        pw.println("</challenge>");

    }

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }
}
