package importer;

import org.xml.sax.SAXException;
import picocli.CommandLine.Parameters;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public abstract class ImportProblemAbstract extends ImportAbstract {
    @Parameters(index = "0", paramLabel = "<prob-id-pref>",
            description = { "Provide problem-id prefix or 'auto' for problem-id generation",
                    "'com.codeforces.polygon.{problem owner}.{problem short name}'"}) String problemIdPrefix;

    @Override
    protected void makeImport() throws IOException, ParserConfigurationException, SAXException {
        importProblem(problemIdPrefix, prepareProblemDirectory(), asker);
    }

    abstract String prepareProblemDirectory() throws IOException;
}
