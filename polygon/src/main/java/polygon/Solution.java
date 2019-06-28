package polygon;

import org.w3c.dom.NodeList;

import org.w3c.dom.Element;
import polygon.properties.SolutionTag;

public class Solution {
    SolutionTag tag;

    String sourcePath;
    String sourceType;
    String ext;

    public Solution(String tag, String path, String type) {
        this.tag = SolutionTag.parse(tag);
        sourcePath = path;
        sourceType = type;
        ext = path.substring(path.lastIndexOf(".") + 1);
    }

    public static Solution[] parse(Element el) {
        NodeList nodeList = el.getElementsByTagName("solution");
        Solution[] solutions = new Solution[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element sol = (Element) nodeList.item(i);
            solutions[i] = new Solution(sol.getAttribute("tag"),
                    ((Element) sol.getElementsByTagName("source").item(0)).getAttribute("path"),
                    ((Element) sol.getElementsByTagName("source").item(0)).getAttribute("type"));
        }
        return solutions;
    }

    public String getSourcePath() {
        return sourcePath;
    }
}
