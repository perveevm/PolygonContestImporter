package polygon;

import polygon.properties.SolutionTag;
import xmlwrapper.XMLElement;

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

    public static Solution[] parse(XMLElement el) {
        XMLElement[] solutionsList = el.findChildren("solution");
        Solution[] solutions = new Solution[solutionsList.length];
        for (int i = 0; i < solutionsList.length; i++) {
            XMLElement sol = solutionsList[i];
            XMLElement source = sol.findFirstChild("source");
            solutions[i] = new Solution(sol.getAttribute("tag"),
                    source.getAttribute("path"),
                    source.getAttribute("type"));
        }
        return solutions;
    }

    public String getSourcePath() {
        return sourcePath;
    }
}
