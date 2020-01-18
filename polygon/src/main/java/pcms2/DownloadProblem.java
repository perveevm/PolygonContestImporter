package pcms2;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.*;

@Command(name = "download-problem", description = "Downloads single problem")
public class DownloadProblem extends ImportProblemAbstract {
    @Parameters(index = "1") String polygonUrl;

    @Override
    String prepareProblemDirectory() throws IOException  {
        File probDir = fileManager.createTemporaryDirectory("__problem");
        return downloadProblemDirectory(polygonUrl, probDir);
    }
}
