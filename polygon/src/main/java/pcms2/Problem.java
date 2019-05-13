package pcms2;

import org.apache.commons.io.FileUtils;
import polygon.properties.PointsPolicy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by Ilshat on 11/22/2015.
 */
public class Problem {
    File directory;
    File xmlFile;
    String id;
    String shortName;
    //testset name -> testset
    //possible testset names - preliminary, main
    TreeMap <String, Testset> testsets;
    ArrayList<Attachment> attachments;
    ArrayList<Solution> solutions;

    Verifier verifier;
    Interactor interactor;

    public Problem(polygon.Problem polygonProblem, String idprefix, Properties languageProps, Properties executableProps) {
        directory = polygonProblem.getDirectory();
        xmlFile = new File(directory, "problem.xml");
        id = idprefix;
        testsets = new TreeMap<>();

        parse(polygonProblem, languageProps, executableProps);
    }

    public void parse(polygon.Problem polygonProblem, Properties languageProps, Properties executableProps) {
        shortName = polygonProblem.getShortName();
        System.out.println("importing problem '" + shortName + "'");

        if (id.startsWith("com.codeforces.polygon") || id.equals("auto")) {
            String[] t = polygonProblem.getUrl().split("/");
            String cflogin = t[t.length - 2];
            if (cflogin.contains(".")) {
                System.out.println("WARNING: Problem owner login contains '.', replacing with '-'");
                cflogin = cflogin.replaceAll("\\.", "-");
            }
            id = "com.codeforces.polygon." + cflogin;
        }
        id = id + "." + shortName;

        //judging
        String input = polygonProblem.getInput();
        if (input.isEmpty()) input = "*";
        String output = polygonProblem.getOutput();
        if (output.isEmpty()) output = "*";

        //testset
        //todo: null pointer if there is no testset named tests
        Testset maints = Testset.parse(polygonProblem.getTestsets().get("tests"), input, output);

        for (Group group : maints.groups) {
            if (group.hasSampleTests) continue;
            int tcount = group.last - group.first + 1;
            String pg = polygonProblem.getTestsets().get("tests").getTests()[group.first].getGroup();
            PointsPolicy pp = polygonProblem.getTestsets().get("tests").getGroups().get(pg).getPointsPolicy();
            if (group.points != null) {
                int[] parsedPoints = Group.getNumbersArray(group.points);
                if (tcount != parsedPoints.length && parsedPoints.length != 1) {
                    System.out.printf("WARNING: Group points can't be distributed between tests correctly for group '%s'", pg);
                    continue;
                }
                if (tcount == parsedPoints.length) {
                    for (int i = group.first; i <= group.last ; i++) {
                        maints.tests[i].points = parsedPoints[i - group.first];
                    }
                } else {
                    distributePoints(maints.tests, group.first, group.last, parsedPoints[0]);
                }
            } else if (pp == PointsPolicy.EACH_TEST) {
                int zeroPoints = 0;
                for (int i = group.first; i <= group.last; i++) {
                    if (maints.tests[i].points == 0) {
                        zeroPoints++;
                    }
                }
                if (zeroPoints > 0) {
                    int sum = (int) polygonProblem.getTestsets().get("tests").getGroups().get(pg).getPoints();

                    if (sum < tcount) {
                        System.out.printf("WARNING: Group points can't be distributed between tests correctly for group '%s'", pg);
                        continue;
                    }

                    distributePoints(maints.tests, group.first, group.last, sum);
                }
            }
        }
        testsets.put("main", maints);

        Testset preliminary;
        if (polygonProblem.getTestsets().containsKey("preliminary")) {
            preliminary = Testset.parse(polygonProblem.getTestsets().get("preliminary"), input, output);
        } else {
            System.out.println("INFO: No preliminary testset, getting sample tests");
            int sampleCount = polygonProblem.getTestsets().get("tests").getSampleTestCount();
            Test[] temp = new Test[sampleCount];
            for (int i = 0; i < sampleCount; i++) {
                temp[i] = maints.tests[i];
            }
            preliminary = new Testset();
            preliminary.tests = temp;
            preliminary.name = "preliminary";
            preliminary.inputName = maints.inputName;
            preliminary.outputName = maints.outputName;
            preliminary.inputHref = maints.inputHref;
            preliminary.outputHref = maints.outputHref;
            preliminary.memoryLimit = maints.memoryLimit;
            preliminary.timeLimit = maints.timeLimit;
        }
        testsets.put("preliminary", preliminary);

        //files attachments
        attachments = Attachment.parse(polygonProblem.getAttachments(), languageProps, shortName);

        //assets (checker)
        verifier = Verifier.parse(polygonProblem.getChecker(), executableProps);

        interactor = Interactor.parse(polygonProblem.getInteractor(), executableProps);
        if (interactor != null) {
            for (Testset e : testsets.values()) {
                e.inputName = shortName + ".in";
                e.outputName = shortName + ".out";
            }
        }

        solutions = Solution.parse(polygonProblem.getSolutions());
    }

    private static void distributePoints(Test[] tests, int first, int last, int sum) {
        int tcount = last - first + 1;
        for (int i = first; i < first + tcount - sum % tcount; i++) {
            tests[i].points = sum / tcount;
        }
        for (int i = first + tcount - sum % tcount; i <= last; i++) {
            tests[i].points = sum / tcount + 1;
        }
    }
    public void print(PrintWriter pw) {
        pw.println("<?xml version = \"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<problem");
        pw.println("\tversion = \"1.0\"");
        pw.println("\tid = \"" + id + "\"");
        pw.println(">");
        pw.println("\t<judging>");

        pw.println("\t\t<script type = \"%" + "icpc" + "\">");

        Testset testset = testsets.get("main");
        if (testset != null) {
            testset.print(pw, "\t\t\t", "icpc");
        } else {
            System.out.println("WARNING: Testset 'tests' not found! This is the main testset in PCMS.");
        }

        verifier.print(pw, "\t\t\t");
        if (interactor != null) {
            interactor.print(pw, "\t\t\t");
        }
        for (Attachment at : attachments) {
            at.print(pw, "\t\t\t");
        }
        pw.println("\t\t</script>");

        pw.println("\t\t<script type = \"%" + "ioi" + "\">");
        testset = testsets.get("preliminary");
        if (testset != null) {
            testset.print(pw, "\t\t\t", "ioi");
        }
        testset = testsets.get("main");
        if (testset != null) {
            testset.print(pw, "\t\t\t", "ioi");
        } else {
            System.out.println("WARNING: Testset 'tests' not found! This is the main testset in PCMS.");
        }

        verifier.print(pw, "\t\t\t");
        if (interactor != null) {
            interactor.print(pw, "\t\t\t");
        }
        for (Attachment at : attachments) {
            at.print(pw, "\t\t\t");
        }
        pw.println("\t\t</script>");

        pw.println("\t</judging>");
        pw.println("</problem>");
    }

    void printSolutions(PrintWriter pw, String sessionId, String problemAlias, Properties languageProperties, String vfs) {
        for (Solution sol : solutions) {
            sol.print(pw, sessionId, problemAlias, languageProperties,
                    vfs + "/problems/" + id.replaceAll("\\.", "/"));
        }
    }

    public boolean copyToVFS(String vfs, BufferedReader in, boolean update) throws IOException {
        File src = xmlFile.getParentFile();
        File dest = new File(vfs + "/problems/" + id.replaceAll("\\.", "/"));
        //System.out.println("DEBUG: src = '" + src.getAbsolutePath() + " dest = '" + dest.getAbsolutePath() + "'");
        if (dest.exists()) {
            System.out.println("Problem '" + dest.getAbsolutePath() + "' exists.");
            String yn = "n";
            if (!update) {
                System.out.println("Do You want to update it?\n(y - yes, yy - yes to all, n - no)");
                yn = in.readLine();
                if (yn.equals("yy")) {
                    update = true;
                }
            }
            if (update || yn.equals("y")) {
                System.out.println("Updating...");
                FileUtils.copyDirectory(src, dest);
            } else {
                System.out.println("Skipping...");
            }
        } else {
            System.out.println("Copying problem '" + dest.getAbsolutePath() + "'.");
            FileUtils.copyDirectory(src, dest);
        }
        return update;
    }
}
