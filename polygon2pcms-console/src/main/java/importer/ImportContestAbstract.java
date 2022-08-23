package importer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pcms2.deployer.DeployerConfig;
import pcms2.Challenge;
import pcms2.Problem;
import picocli.CommandLine.Parameters;
import polygon.ContestDescriptor;
import polygon.ProblemDescriptor;
import polygon.Solution;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.NavigableMap;

public abstract class ImportContestAbstract extends ImportAbstract {
    private final static Logger logger = LogManager.getLogger(ImportContestAbstract.class);
    @Parameters(index = "0", paramLabel = "<challenge-id>",
            description = { "Provide challenge-id (ru.demo.day1), it would be used as problem-id prefix (ru.demo.day1.aplusb) ",
                    "or 'auto' for challenge-id generation: 'com.codeforces.polygon.{contest id}' for challenge-id",
                    "and 'com.codeforces.polygon.{problem owner}.{problem short name}' for problem-id"}) String challengeId;
    @Parameters(index = "1", paramLabel = "<type>", description = "Use ioi or icpc") String challengeType;

    protected void importContest(File contestDirectory, ContestDescriptor contest, NavigableMap<String, Problem> pcmsProblems) throws IOException {
        Asker copyToVfsAsker = asker.copyAsker();
        copyToVfsAsker.setAskForAll(true);
        DeployerConfig copyToVfsConfig = new DeployConfigAsker(copyToVfsAsker);
        for (Problem problem : pcmsProblems.values()) {
            copyProblemToVfs(problem, copyToVfsConfig);
        }

        Challenge challenge = new Challenge(contest, challengeId, challengeType, defaultLanguage);
        File challengeXMLFile = new File(contestDirectory, "challenge.xml");
        challenge.print(challengeXMLFile);
        if (vfs != null) {
            deployer.deployChallengeXML(challengeXMLFile, challenge.getId(), copyToVfsConfig);
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
            deployer.deploySubmitLst(submitListFile, challenge.getId(), copyToVfsConfig);
        }

        if (webroot != null) {
            deployer.copyToWEB(contestDirectory, challenge, new DeployConfigAsker(asker));
        }

        logger.info("Contest directory: " + contestDirectory.getAbsolutePath());
    }
}
