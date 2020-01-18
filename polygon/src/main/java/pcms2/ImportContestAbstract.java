package pcms2;

import picocli.CommandLine.Parameters;
import polygon.ContestDescriptor;
import polygon.ProblemDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.NavigableMap;

public abstract class ImportContestAbstract extends ImportAbstract {
    @Parameters(index = "0", paramLabel = "<challenge-id>",
            description = { "Provide challenge-id (ru.demo.day1), it would be used as problem-id prefix (ru.demo.day1.aplusb) ",
                    "or 'auto' for challenge-id generation: 'com.codeforces.polygon.{contest id}' for challenge-id",
                    "and 'com.codeforces.polygon.{problem owner}.{problem short name}' for problem-id"}) String challengeId;
    @Parameters(index = "1", paramLabel = "<type>", description = "Use ioi or icpc") String challengeType;

    protected void importContest(File contestDirectory, ContestDescriptor contest, NavigableMap<String, ProblemDescriptor> contestProblems) throws IOException {
        Challenge challenge = new Challenge(contest, contestProblems, challengeId, challengeType, contestDirectory.getAbsolutePath(), languageProps, executableProps, defaultLanguage);
        try (PrintWriter pw = new PrintWriter(new File(contestDirectory, "challenge.xml"))) {
            challenge.print(pw);
        }

        for (Problem problem : challenge.problems.values()) {
            generateTemporaryProblemXML(problem);
        }

        boolean update = updateAll;
        for (Problem problem : challenge.problems.values()) {
            update = finalizeImportingProblem(problem, update);
        }
        if (vfs != null) {
            File submitListFile = new File(contestDirectory, "submit.lst");
            try (PrintWriter submit = new PrintWriter(submitListFile)) {
                for (Map.Entry<String, Problem> entry : challenge.problems.entrySet()) {
                    entry.getValue().printSolutions(submit, challenge.id + ".0", entry.getKey().toUpperCase(), languageProps, vfs);
                }
            }
        }

        if (vfs != null) {
            challenge.copyToVFS(vfs, sysin, update);
        }

        if (webroot != null) {
            challenge.copyToWEB(webroot, sysin, updateAll);
        }

        System.out.println("Contest directory: " + contestDirectory.getAbsolutePath());
    }
}
