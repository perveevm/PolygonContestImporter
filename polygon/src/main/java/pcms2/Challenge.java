package pcms2;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by Ilshat on 11/24/2015.
 */
public class Challenge {
    String path;
    String url;
    String name;
    //language -> name
    TreeMap <String, String> names;
    String id;
    String type;
    TreeMap<String, Problem> problems;

    public Challenge() {
        problems = new TreeMap<>();
        path = "";
    }

    public Challenge(String ID, String Type, String Path, Properties languageProps, Properties executableProps, String defaultLang) throws Exception {
        problems = new TreeMap<>();
        names = new TreeMap<>();
        path = Path;
        id = ID;
        type = Type;
        parse(languageProps, executableProps, defaultLang);
    }

    void parse(Properties languageProps, Properties executableProps, String defaultLang) throws Exception {
        System.out.println("parsing contest.xml ...");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(path, "contest.xml"));
        //NodeList problem = doc.getDocumentElement().getChildNodes();
        Element el = doc.getDocumentElement();
        url = el.getAttribute("url");
        if (id.equals("auto")) {
            String[] t = url.split("/");
            id = "com.codeforces.polygon." + t[t.length - 1];
        }

        //names
        Element child = (Element) el.getElementsByTagName("names").item(0);
        NodeList nl = child.getElementsByTagName("name");
        for (int i = 0; i < nl.getLength(); i++) {
            child = (Element) nl.item(i);
            names.put(child.getAttribute("language"), child.getAttribute("value"));
        }
        if (names.containsKey(defaultLang)) {
            name = names.get(defaultLang);
        } else {
            name = names.firstEntry().getValue();
            System.out.println("WARNING: Challenge name for default language '" + defaultLang + "' not found! Using '" + names.firstKey() + "' name.");
        }

        child = (Element) el.getElementsByTagName("problems").item(0);
        nl = child.getElementsByTagName("problem");
        for (int i = 0; i < nl.getLength(); i++) {
            child = (Element) nl.item(i);
            String purl = child.getAttribute("url");
            String index = child.getAttribute("index");
            String pname = purl.substring(purl.lastIndexOf("/") + 1);
            Problem p = new Problem(new File(path, "problems/" + pname).getAbsolutePath(), id, type,
                    languageProps, executableProps, defaultLang);
            if (!p.url.equals(purl)) {
                System.out.println("ERROR: Problem URL do not match! Contest problem = '" + purl + "' problems.xml = '" + p.url + "'");
                System.exit(1);
            }
            problems.put(index, p);
        }
    }

    public void print(PrintWriter pw) {
        pw.println("<?xml version = \"1.0\" encoding = \"windows-1251\" ?>");
        pw.println("<challenge");
        pw.println("\tid = \"" + id + "\"");
        pw.println("\tname = \"" + name + "\"");
        pw.println("\tscoring-model = \"%" + type + "\"");
        pw.println("\tscoring-mode = \"group-max\"");
        pw.println("\txmlai-process = \"http://neerc.ifmo.ru/develop/pcms2/xmlai/default-rules.xml\"");
        pw.println(">");
        for (Map.Entry<String, Problem> e : problems.entrySet()) {
            pw.println("\t<problem-ref alias = \"" + e.getKey().toUpperCase() + "\" " +
                    "id = \"" + e.getKey() + "\" " +
                    "problem-id = \"" + e.getValue().id + "\" " +
                    "name = \"" + e.getValue().name + "\"/>");
        }
        pw.println("</challenge>");

    }

    public boolean copyToVFS(String vfs, BufferedReader in, boolean update) throws IOException {
        String[] files = {"challenge.xml", "submit.lst"};
        for (String f : files) {
            File src = new File(path, f);
            File dest = new File(vfs + "/etc/" + id.replaceAll("\\.", "/") + "/" + f);
            //System.out.println("DEBUG: src = '" + src.getAbsolutePath() + " dest = '" + dest.getAbsolutePath() + "'");
            if (dest.exists()) {
                System.out.println(f + " '" + dest.getAbsolutePath() + "' exists.");
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
                System.out.println("Copying " + f + " to '" + dest.getAbsolutePath() + "'.");
                FileUtils.copyFileToDirectory(src, dest.getParentFile());
            }
        }
        return update;
    }

    public void copyToWEB(String webroot, BufferedReader in) throws IOException {
        File src = new File(path, "statements/russian/statements.pdf");
        File dest = new File(webroot + "/statements/" + id.replaceAll("\\.", "/") + "/statements.pdf");
        //System.out.println("DEBUG: src = '" + src.getAbsolutePath() + " dest = '" + dest.getAbsolutePath() + "'");
        if (src.exists()) {
            System.out.println("Statements '" + src.getAbsolutePath() + "' exists.");
            String yn = "n";
            System.out.println("Do You want to publish it?\n(y - yes, n - no)");
            yn = in.readLine();

            if (yn.equals("y")) {
                System.out.println("Publishing...");
                FileUtils.copyFileToDirectory(src, dest.getParentFile());
            } else {
                System.out.println("Skipping...");
            }
        }
    }
}
