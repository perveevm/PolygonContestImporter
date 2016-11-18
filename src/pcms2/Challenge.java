package pcms2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Ilshat on 11/24/2015.
 */
public class Challenge {
    public String path;
    public String url;
    public String name;
    public String id;
    public String type;
    public TreeMap<String, Problem> problems;

    public Challenge() {
        problems = new TreeMap<>();
        path = "";
    }

    public Challenge(String ID, String Type, String Path) throws Exception {
        problems = new TreeMap<>();
        path = Path;
        id = ID;
        type = Type;
        parse();
    }

    public void parse() throws Exception {
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
        Element child = (Element) el.getElementsByTagName("names").item(0);
        NodeList nl = child.getElementsByTagName("name");
        for (int i = 0; i < nl.getLength(); i++) {
            child = (Element) nl.item(i);
            if (child.getAttribute("language").equals("russian")) {
                name = child.getAttribute("value");
            }
        }
        child = (Element) el.getElementsByTagName("problems").item(0);
        nl = child.getElementsByTagName("problem");
        for (int i = 0; i < nl.getLength(); i++) {
            child = (Element) nl.item(i);
            String purl = child.getAttribute("url");
            String index = child.getAttribute("index");
            String pname = purl.substring(purl.lastIndexOf("/") + 1);
            Problem p = new Problem(new File(path, "problems/" + pname).getAbsolutePath(), id, type);
            if (!p.url.equals(purl)) {
                System.out.println("Problem URL do not match! Contest problem = '" + purl + "' problems.xml = '" + p.url + "'");
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
                    "problem-id = \"" + e.getValue().ID + "\" " +
                    "name = \"" + e.getValue().Name + "\"/>");
        }
        pw.println("</challenge>");

    }
}
