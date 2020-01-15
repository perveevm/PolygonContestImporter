package pcms2;

import org.xml.sax.SAXException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import polygon.Contest;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Command(name = "challenge", aliases = {"contest"}, description = "Imports whole contest using contest.xml")
public class ImportChallenge extends ImportAbstract {
    @Parameters(index = "0", paramLabel = "<challenge-id>",
            description = { "Provide challenge-id (ru.demo.day1), it would be used as problem-id prefix (ru.demo.day1.aplusb) ",
                    "or 'auto' for challenge-id generation: 'com.codeforces.polygon.{contest id}' for challenge-id",
                    "and 'com.codeforces.polygon.{problem owner}.{problem short name}' for problem-id"}) String challengeId;
    @Parameters(index = "1", paramLabel = "<type>", description = "Use ioi or icpc") String challengeType;
    @Parameters(index = "2", arity = "0..1", description = "Path to directory that contains 'contest.xml'", defaultValue = ".") String folder;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        Contest contest = Contest.parse(folder);
        Challenge ch = new Challenge(contest, challengeId, challengeType, folder, languageProps, executableProps, defaultLanguage);
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(folder, "challenge.xml")))) {
            ch.print(pw);
        }

        for (Problem pr : ch.problems.values()) {
            generateTemporaryProblemXML(pr);
        }

        boolean update = updateAll;
        for (Problem problem : ch.problems.values()) {
            update = finalizeImportingProblem(problem, update);
        }

        if (vfs != null) {
            try (PrintWriter submit = new PrintWriter(new File(folder, "submit.lst"))) {
                for (Map.Entry<String, Problem> entry : ch.problems.entrySet()) {
                    entry.getValue().printSolutions(submit, ch.id + ".0", entry.getKey().toUpperCase(), languageProps, vfs);
                }
            }
        }

        if (vfs != null) {
            ch.copyToVFS(vfs, sysin, update);
        }

        if (webroot != null) {
            ch.copyToWEB(webroot, sysin, updateAll);
        }
    }
}
