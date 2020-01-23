package importer;

import org.xml.sax.SAXException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import polygon.ContestDescriptor;
import polygon.ProblemDescriptor;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

@Command(name = "challenge", aliases = {"contest"}, description = "Imports whole contest using contest.xml")
public class ImportChallenge extends ImportContestAbstract {
    @Parameters(index = "2", arity = "0..1", description = "Path to directory that contains 'contest.xml'", defaultValue = ".") String folder;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        File contestDirectory = new File(folder);
        ContestDescriptor contest = ContestDescriptor.parse(new File(contestDirectory, "contest.xml"));
        //problem index maps to problem
        TreeMap<String, ProblemDescriptor> problems = new TreeMap<>();
        for (Map.Entry<String, String> entry : contest.getProblemLinks().entrySet()) {
            String purl = entry.getValue();
            String index = entry.getKey();
            String pname = purl.substring(purl.lastIndexOf("/") + 1);
            ProblemDescriptor problem = ProblemDescriptor.parse(new File(contestDirectory, "problems/" + pname).getAbsolutePath());
            if (!problem.getUrl().equals(purl)) {
                System.out.println("ERROR: Problem URL do not match! Contest problem = '" + purl + "' problems.xml = '" + problem.getUrl() + "'");
                System.exit(1);
            }
            problems.put(index, problem);
        }
        importContest(contestDirectory, contest, problems);
    }
}
