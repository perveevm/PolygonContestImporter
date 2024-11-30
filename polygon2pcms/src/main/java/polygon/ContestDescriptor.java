package polygon;

import org.xml.sax.SAXException;
import ru.perveevm.polygon.api.entities.Problem;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Function;

public class ContestDescriptor extends ContestXML {
    protected NavigableMap<String, ProblemDescriptor> problems;

    public ContestDescriptor(File xmlFile, NavigableMap<String, ProblemDescriptor> problems) throws IOException, SAXException, ParserConfigurationException {
        super(xmlFile);
        this.problems = problems;
    }

    public ContestDescriptor(Map<String, Problem> contestProblems, NavigableMap<String, ProblemDescriptor> problems) throws IOException, SAXException, ParserConfigurationException {
        super(contestProblems);
        this.problems = problems;
    }

    public ContestDescriptor(InputStream stream,
                             Function<ContestXML, NavigableMap<String, ProblemDescriptor>> getProblems)
            throws IOException, SAXException, ParserConfigurationException {
        super(stream);
        this.problems = getProblems.apply(this);
    }

    public NavigableMap<String, ProblemDescriptor> getProblems() {
        return problems;
    }
}
