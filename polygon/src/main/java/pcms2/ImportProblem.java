package pcms2;

import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

@Command(name = "problem", description = "Imports single problem using problem.xml")
public class ImportProblem extends ImportProblemAbstract {
    @Parameters(index = "1", arity = "0..1", description = "Path to directory, that contains 'problem.xml'", defaultValue = ".") String folder;

    @Override
    String prepareProblemDirectory() {
        return folder;
    }
}
