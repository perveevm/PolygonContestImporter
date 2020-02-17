package pcms2;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;

public class Solution {

    String sourcePath;
    String ext;

    public Solution(String path) {
        sourcePath = path;
        ext = path.substring(path.lastIndexOf(".") + 1);
    }

    void print(PrintWriter pw, String sessionId, String problemAlias, Properties languageProperties, String problemPath) {
        if (languageProperties.getProperty(ext) != null) {
            String[] langs = languageProperties.getProperty(ext).split(",");
            for (String lang : langs) {
                pw.printf("%s %s %s 1s %s/%s\n", sessionId, problemAlias, lang, problemPath, sourcePath);
            }
        }
    }

    static ArrayList<Solution> parse(ArrayList<polygon.Solution> polygonSolutions) {
        if (polygonSolutions == null) return null;

        ArrayList<Solution> solutions = new ArrayList<>();
        for (polygon.Solution polygonSolution : polygonSolutions) {
            solutions.add(new Solution(polygonSolution.getSourcePath()));
        }
        return solutions;
    }
}
