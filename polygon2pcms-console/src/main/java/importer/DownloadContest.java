package importer;

import polygon.download.PolygonPackageType;
import org.xml.sax.SAXException;
import pcms2.Problem;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import polygon.ContestDescriptor;
import polygon.ContestXML;
import polygon.ProblemDescriptor;
import polygon.ProblemDirectory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static picocli.CommandLine.*;

@Command(name = "download-contest", description = "Downloads whole contest")
public class DownloadContest extends ImportContestAbstract {
    @Parameters(index = "2") String uid;
    @Option(names = "--download", description = "Defines download strategy: 'all' downloads all problem packages, " +
            "'new' downloads only problem packages with different from vfs revision") DownloadStrategy downloadStrategy;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        File contestDirectory = fileManager.createTemporaryDirectory("contest-" + uid + "-");
        File contestXMLFile = new File(contestDirectory, "contest.xml");
        if (username == null || password == null) {
            throw new AssertionError("Polygon username or password is not set");
        }
        if (!downloader.downloadContestXml(uid, contestXMLFile)) {
            throw new AssertionError("Couldn't download contest.xml for contest " + uid);
        }
        File problemsDirectory = new File(contestDirectory, "problems");
        if (!problemsDirectory.mkdir()) {
            throw new AssertionError("Couldn't create problems directory " + problemsDirectory.getAbsolutePath());
        }
        if (downloadStrategy == null) {
            downloadStrategy = DownloadStrategy.valueOf(importProps.getProperty("download", "new").toUpperCase());
        }
        ContestXML contestXML = ContestXML.parse(contestXMLFile);
        NavigableMap<String, ProblemDescriptor> problemDescriptors = new TreeMap<>();
        NavigableMap<String, Problem> pcmsProblems = new TreeMap<>();
        for (Map.Entry<String, String> entry : contestXML.getProblemLinks().entrySet()) {
            String index = entry.getKey();
            String url = entry.getValue();
            String pname = url.substring(url.lastIndexOf("/") + 1);
            boolean download = true;
            ProblemDescriptor newProblemDescriptor = null;
            if (downloadStrategy == DownloadStrategy.NEW && vfs != null) {
                File problemXmlFile = fileManager.createTemporaryFile("_p_" + entry.getKey() + "_", ".xml");
                if (!downloader.downloadProblemXml(url, problemXmlFile)) {
                    throw new AssertionError("Couldn't download problem.xml by url '" + url + "'");
                }
                newProblemDescriptor = ProblemDescriptor.parse(problemXmlFile);
                fileManager.remove(problemXmlFile);
                String problemId = Problem.getProblemId(challengeId, newProblemDescriptor.getUrl(), newProblemDescriptor.getShortName());
                if (newProblemDescriptor.getRevision() == deployer.getVfsProblemRevision(problemId)) {
                    download = false;
                    System.out.println("INFO: VFS revision is same, skipping package download");
                }
                if (download) {
                    System.out.println("INFO: VFS revision is different, downloading package");
                }
            }
            if (download) {
                File problemDirectory = new File(problemsDirectory, pname);
                PolygonPackageType packageType = downloader.downloadProblemDirectory(url, problemDirectory, fileManager);
                Problem pcmsProblem = converter.convertProblem(problemDirectory, challengeId, packageType == PolygonPackageType.STANDARD);
                pcmsProblems.put(index, pcmsProblem);
                problemDescriptors.put(index, pcmsProblem.getPolygonProblem());
            } else {
                problemDescriptors.put(index, newProblemDescriptor);
            }
        }
        File statementDirectory = new File(contestDirectory, "statements");
        if (!statementDirectory.mkdir()) {
            throw new AssertionError("Couldn't create directory " + statementDirectory.getAbsolutePath());
        }
        for (Map.Entry<String, String> entry : contestXML.getStatementLinks().entrySet()) {
            File languageDirectory = new File(statementDirectory, entry.getKey());
            if (!languageDirectory.mkdir()) {
                throw new AssertionError("Couldn't create directory " + languageDirectory.getAbsolutePath());
            }
            File statementFile = new File(languageDirectory, "statements.pdf");
            downloader.downloadByURL(entry.getValue(), statementFile);
        }
        importContest(contestDirectory, new ContestDescriptor(contestXMLFile, problemDescriptors), pcmsProblems);
    }

}
