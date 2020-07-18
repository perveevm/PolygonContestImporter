package importer;

import polygon.download.PolygonPackageType;
import org.xml.sax.SAXException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

@Command(name = "download-problem", description = "Downloads single problem")
public class DownloadProblem extends ImportProblemAbstract {
    @Parameters(index = "1") String polygonUrl;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        File probDir = fileManager.createTemporaryDirectory("__problem");
        PolygonPackageType packageType = downloader.downloadProblemDirectory(polygonUrl, probDir, fileManager);
        convertAndCopy(problemIdPrefix, probDir, asker, packageType == PolygonPackageType.STANDARD);
    }
}
