package importer;

import org.xml.sax.SAXException;
import pcms2.Problem;
import picocli.CommandLine.Parameters;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public abstract class ImportProblemAbstract extends ImportAbstract {
    @Parameters(index = "0", paramLabel = "<prob-id-pref>",
            description = { "Provide problem-id prefix or 'auto' for problem-id generation",
                    "'com.codeforces.polygon.{problem owner}.{problem short name}'"}) String problemIdPrefix;

    /**
     * Takes extracted polygon package directory and converts it to PCMS2 problem directory.
     * Copies problem to VFS if needed.
     *
     * @param problemIdPrefix prefix to construct PCMS2 problem-id
     * @param folder          polygon package directory to process
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    protected void convertAndCopy(String problemIdPrefix, File folder, Asker asker, boolean runDoAll) throws IOException, ParserConfigurationException, SAXException {
        Problem pi = converter.convertProblem(folder, problemIdPrefix, runDoAll);
        copyProblemToVfs(pi, new DeployConfigAsker(asker));
    }
}
