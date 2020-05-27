package polygon;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.NavigableMap;

public class ContestDescriptor extends ContestXML {
    protected NavigableMap<String, ProblemDescriptor> problems;

    public ContestDescriptor(File xmlFile, NavigableMap<String, ProblemDescriptor> problems) throws IOException, SAXException, ParserConfigurationException {
        super(xmlFile);
        this.problems = problems;
        parseXML();
    }

    public NavigableMap<String, ProblemDescriptor> getProblems() {
        return problems;
    }
}
