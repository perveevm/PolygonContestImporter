package importer;

import pcms2.Challenge;
import pcms2.Problem;
import picocli.CommandLine.Parameters;
import polygon.ContestDescriptor;
import polygon.ProblemDescriptor;
import polygon.ProblemDirectory;
import polygon.Solution;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class ImportContestAbstract extends ImportAbstract {
    @Parameters(index = "0", paramLabel = "<challenge-id>",
            description = { "Provide challenge-id (ru.demo.day1), it would be used as problem-id prefix (ru.demo.day1.aplusb) ",
                    "or 'auto' for challenge-id generation: 'com.codeforces.polygon.{contest id}' for challenge-id",
                    "and 'com.codeforces.polygon.{problem owner}.{problem short name}' for problem-id"}) String challengeId;
    @Parameters(index = "1", paramLabel = "<type>", description = "Use ioi or icpc") String challengeType;

    protected void importContest(File contestDirectory, ContestDescriptor contest, NavigableMap<String, ProblemDirectory> polygonProblems) throws IOException {
        Challenge challenge = new Challenge(contest, challengeId, challengeType, defaultLanguage);
        challenge.print(new File(contestDirectory, "challenge.xml"));

        NavigableMap<String, Problem> pcmsProblems = polygonProblems.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new Problem(entry.getValue(), challengeId, languageProps, executableProps), (x, y) -> y, TreeMap::new));
        for (Problem problem : pcmsProblems.values()) {
            processProblem(problem);
        }

        Asker copyToVfsAsker = asker.copyAsker();
        copyToVfsAsker.setAskForAll(true);
        for (Problem problem : pcmsProblems.values()) {
            finalizeImportingProblem(problem, copyToVfsAsker);
        }
        if (vfs != null) {
            File submitListFile = new File(contestDirectory, "submit.lst");
            try (PrintWriter submit = new PrintWriter(submitListFile)) {
                for (Map.Entry<String, ProblemDescriptor> entry : contest.getProblems().entrySet()) {
                    ProblemDescriptor polygonProblem = entry.getValue();
                    String problemId = Problem.getProblemId(challengeId, polygonProblem.getUrl(), polygonProblem.getShortName());
                    String directory = vfs + "/problems/" + problemId.replace(".", "/");
                    for (Solution sol : polygonProblem.getSolutions()) {
                        String sourcePath = sol.getSourcePath();
                        Utils.getLanguagesBySourcePath(sourcePath, languageProps).forEach(lang -> {
                            String sessionId = challenge.getId() + ".0";
                            String problemAlias = entry.getKey().toUpperCase();
                            submit.printf("%s %s %s 1s %s/%s\n", sessionId, problemAlias, lang, directory, sourcePath);
                        });
                    }
                }
            }
        }

        if (vfs != null) {
            Utils.copyToVFS(contestDirectory, challenge, vfs, copyToVfsAsker);
        }

        if (webroot != null) {
            Utils.copyToWEB(contestDirectory, challenge, webroot, asker);
        }

        System.out.println("Contest directory: " + contestDirectory.getAbsolutePath());
    }
}
