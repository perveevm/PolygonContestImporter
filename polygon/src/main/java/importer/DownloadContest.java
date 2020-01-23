package importer;

import org.xml.sax.SAXException;
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

@Command(name = "download-contest", description = "Downloads whole contest")
public class DownloadContest extends ImportContestAbstract {
    @Parameters(index = "2") String uid;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        File contestDirectory = fileManager.createTemporaryDirectory("contest-" + uid + "-");
        File contestXMLFile = new File(contestDirectory, "contest.xml");
        if (!downloader.downloadContestXml(uid, contestXMLFile)) {
            throw new AssertionError("Couldn't download contest.xml for contest " + uid);
        }
        File problemsDirectory = new File(contestDirectory, "problems");
        if (!problemsDirectory.mkdir()) {
            throw new AssertionError("Couldn't create problems directory " + problemsDirectory.getAbsolutePath());
        }
        ContestDescriptor contest = ContestDescriptor.parse(contestXMLFile);
        NavigableMap<String, ProblemDescriptor> contestProblems = new TreeMap<>();
        for (Map.Entry<String, String> entry : contest.getProblemLinks().entrySet()) {
            String index = entry.getKey();
            String url = entry.getValue();
            String pname = url.substring(url.lastIndexOf("/") + 1);
            File problemDirectory = new File(problemsDirectory, pname);
            downloadProblemDirectory(url, problemDirectory);
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
