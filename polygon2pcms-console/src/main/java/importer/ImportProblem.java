package importer;

import org.xml.sax.SAXException;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@Command(name = "problem", description = "Imports single problem using problem.xml")
public class ImportProblem extends ImportProblemAbstract {
    @Parameters(index = "1", arity = "0..1", paramLabel = "<package>",
            description = "Path to directory or zip-file, that contains 'problem.xml'", defaultValue = ".") String polygonPackage;
    @Option(names = "--doall", description = "Run doall, before importing") boolean runDoAll;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        File f = new File(polygonPackage);
        File probDir = acquireDirectory(f, fileManager);
        convertAndCopy(problemIdPrefix, probDir, asker, runDoAll);
    }

}
