package importer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import polygon.ContestDescriptor;
import polygon.ProblemDescriptor;
import polygon.download.PolygonPackageType;
import ru.perveevm.polygon.api.entities.Problem;
import ru.perveevm.polygon.exceptions.api.PolygonSessionException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

@CommandLine.Command(name = "download-contest-api", description = "Downloads whole contest via Polygon API")
public class DownloadContestAPI extends ImportContestAbstract {
    private final static Logger log = LogManager.getLogger(DownloadContestAPI.class);
    @CommandLine.Parameters(index = "2") Integer contestId;
    @CommandLine.Option(names = "--download", description = "Defines download strategy: 'all' downloads all problem packages, " +
            "'new' downloads only problem packages with different from vfs revision") DownloadStrategy downloadStrategy;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        File contestDirectory = fileManager.createTemporaryDirectory("contest-" + contestId + "-");
        if (apiKey == null || apiSecret == null) {
            throw new AssertionError("Polygon API key or API secret is not set");
        }
        Map<String, Problem> contestProblems;
        try {
            contestProblems = downloader.getContestProblems(contestId);
        } catch (PolygonSessionException e) {
            throw new AssertionError("Cannot fetch contest problems list: " + e.getMessage());
        }

        File problemsDirectory = new File(contestDirectory, "problems");
        if (!problemsDirectory.mkdir()) {
            throw new AssertionError("Couldn't create problems directory " + problemsDirectory.getAbsolutePath());
        }
        if (downloadStrategy == null) {
            downloadStrategy = DownloadStrategy.valueOf(importProps.getProperty("download", "new").toUpperCase());
        }
        NavigableMap<String, ProblemDescriptor> problemDescriptors = new TreeMap<>();
        NavigableMap<String, pcms2.Problem> pcmsProblems = new TreeMap<>();
        for (Map.Entry<String, Problem> entry : contestProblems.entrySet()) {
            String index = entry.getKey();
            String pname = entry.getValue().getName();

            File problemDirectory = new File(problemsDirectory, pname);
            PolygonPackageType packageType = downloader.downloadProblemDirectoryAPI(entry.getValue().getId(), entry.getValue().getLatestPackage(), problemDirectory, fileManager);
            pcms2.Problem pcmsProblem = converter.convertProblem(problemDirectory, challengeId, packageType == PolygonPackageType.STANDARD);
            pcmsProblems.put(index, pcmsProblem);
            problemDescriptors.put(index, pcmsProblem.getPolygonProblem());
        }
        importContest(contestDirectory, new ContestDescriptor(contestProblems, problemDescriptors), pcmsProblems);
    }
}
