package pcms2;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class Solution {
    private final String sourcePath;
    private final String extension;

    public Solution(String path) {
        sourcePath = path;
        extension = FilenameUtils.getExtension(FilenameUtils.getName(sourcePath));
    }

    static ArrayList<Solution> parse(List<polygon.Solution> polygonSolutions) {
        if (polygonSolutions == null) return null;

        ArrayList<Solution> solutions = new ArrayList<>();
        for (polygon.Solution polygonSolution : polygonSolutions) {
            solutions.add(new Solution(polygonSolution.getSourcePath()));
        }
        return solutions;
    }
}
