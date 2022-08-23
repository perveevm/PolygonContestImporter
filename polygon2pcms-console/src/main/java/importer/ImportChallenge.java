package importer;

import org.xml.sax.SAXException;
import pcms2.Problem;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;
import polygon.ContestDescriptor;
import polygon.ContestXML;
import polygon.ProblemDescriptor;
import polygon.ProblemDirectory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

@Command(name = "challenge", aliases = {"contest"}, description = "Imports whole contest using contest.xml")
public class ImportChallenge extends ImportContestAbstract {
    @Parameters(index = "2", arity = "0..1", description = "Path to directory that contains 'contest.xml'", defaultValue = ".") String folder;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        File contestDirectory = acquireDirectory(new File(folder), fileManager);
        File contestXMLFile = new File(contestDirectory, "contest.xml");
        ContestXML contest = ContestXML.parse(contestXMLFile);
        //problem index maps to problem
        NavigableMap<String, Problem> pcmsProblems = new TreeMap<>();
        NavigableMap<String, ProblemDescriptor> problemDescriptors = new TreeMap<>();
        for (Map.Entry<String, String> entry : contest.getProblemLinks().entrySet()) {
            String purl = entry.getValue();
            String index = entry.getKey();
            String pname = purl.substring(purl.lastIndexOf("/") + 1);
            Problem pcmsProblem = converter.convertProblem(new File(contestDirectory, "problems/" + pname), challengeId, false);
            ProblemDirectory polygonProblem = pcmsProblem.getPolygonProblem();
            if (!polygonProblem.getUrl().equals(purl)) {
                throw new RuntimeException("Problem URL do not match! Contest problem = '" + purl + "' problems.xml = '" + polygonProblem.getUrl() + "'");
            }
            pcmsProblems.put(index, pcmsProblem);
            problemDescriptors.put(index, polygonProblem);
        }
        importContest(contestDirectory, new ContestDescriptor(contestXMLFile, problemDescriptors), pcmsProblems);
    }
}
