package pcms2;

import org.w3c.dom.NodeList;
import pcms2.properties.SolutionTag;

import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.util.Properties;

public class Solution {
    SolutionTag tag;
    String sourcePath;
    String ext;

    public Solution(String tag, String path) {
        this.tag = SolutionTag.parse(tag);
        sourcePath = path;
        ext = path.substring(path.lastIndexOf(".") + 1);
    }

    public static Solution[] parse(Element el) {
        NodeList nodeList = el.getElementsByTagName("solution");
        Solution[] solutions = new Solution[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element sol = (Element) nodeList.item(i);
            solutions[i] = new Solution(sol.getAttribute("tag"),
                    ((Element) sol.getElementsByTagName("source").item(0)).getAttribute("path"));
        }
        return solutions;
    }

    void print(PrintWriter pw, String sessionId, String problemAlias, Properties languageProperties, String problemPath) {
        if (languageProperties.getProperty(ext) != null) {
            String[] langs = languageProperties.getProperty(ext).split(",");
            for (String lang : langs) {
                pw.printf("%s %s %s 1s %s/%s\n", sessionId, problemAlias, lang, problemPath, sourcePath);
            }
        }
    }
}
