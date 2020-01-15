package pcms2;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Files;

@Command(name = "download-problem", description = "Downloads single problem")
public class DownloadProblem extends ImportProblemAbstract {
    @Parameters(index = "1") String polygonUrl;

    @Override
    String prepareProblemDirectory() throws IOException  {
        File probDir = Files.createTempDirectory("__problem").toFile();
        return Utils.downloadProblemDirectory(polygonUrl, probDir, downloader);
    }
}
