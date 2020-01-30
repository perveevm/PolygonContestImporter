package importer;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import pcms2.Problem;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import polygon.ContestDescriptor;
import polygon.ProblemDescriptor;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static picocli.CommandLine.*;

@Command(name = "download-contest", description = "Downloads whole contest")
public class DownloadContest extends ImportContestAbstract {
    @Parameters(index = "2") String uid;
    @Option(names = "--download", description = "Defines download strategy: 'all' downloads all problem packages, " +
            "'new' downloads only problem packages with different from vfs revision") String downloadStrategy;

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
            downloadStrategy = importProps.getProperty("download", "all");
        }
        ContestDescriptor contest = ContestDescriptor.parse(contestXMLFile);
        NavigableMap<String, ProblemDescriptor> contestProblems = new TreeMap<>();
        for (Map.Entry<String, String> entry : contest.getProblemLinks().entrySet()) {
            String index = entry.getKey();
            String url = entry.getValue();
            String pname = url.substring(url.lastIndexOf("/") + 1);
            File problemDirectory = new File(problemsDirectory, pname);
            boolean download = true;
            if (downloadStrategy.equals("new")) {
                File problemXmlFile = new File(problemDirectory, "problem.xml.polygon");
                if (!downloader.downloadProblemXml(url, problemXmlFile)) {
                    throw new AssertionError("Couldn't download problem.xml by url '" + url + "'");
                }
                ProblemDescriptor problemDescriptor = ProblemDescriptor.parse(problemDirectory.getAbsolutePath());
                try {
                    String problemId = Problem.getProblemId(challengeId, problemDescriptor.getUrl(), problemDescriptor.getShortName());
                    ProblemDescriptor problemDescriptorVfs = ProblemDescriptor.parse(vfs + "/problems/" +
                            problemId.replace(".", "/") + "/problem.xml.polygon");
                    if (problemDescriptor.getRevision() == problemDescriptorVfs.getRevision()) {
                        download = false;
                        System.out.println("INFO: VFS revision is same, skipping package download");
                    }
                } catch (Exception ignored) {
                }
                if (download) {
                    FileUtils.deleteQuietly(problemXmlFile);
                    System.out.println("INFO: VFS revision is different, downloading package");
                }
            }
            if (download) {
                downloadProblemDirectory(url, problemDirectory);
            }
            contestProblems.put(index, ProblemDescriptor.parse(problemDirectory.getAbsolutePath()));
        }
        File statementDirectory = new File(contestDirectory, "statements");
        if (!statementDirectory.mkdir()) {
            throw new AssertionError("Couldn't create directory " + statementDirectory.getAbsolutePath());
        }
        for (Map.Entry<String, String> entry : contest.getStatementLinks().entrySet()) {
            File languageDirectory = new File(statementDirectory, entry.getKey());
            if (!languageDirectory.mkdir()) {
                throw new AssertionError("Couldn't create directory " + languageDirectory.getAbsolutePath());
            }
            File statementFile = new File(languageDirectory, "statements.pdf");
            downloader.downloadByURL(entry.getValue(), statementFile);
        }
        importContest(contestDirectory, contest, contestProblems);
    }

}
