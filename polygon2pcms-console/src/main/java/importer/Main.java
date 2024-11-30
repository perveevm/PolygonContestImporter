package importer;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

@Command(name = "polygon-contest-importer", description = "Imports polygon package to PCMS2",
        subcommands = {
            ImportChallenge.class,
            ImportProblem.class,
            DownloadProblem.class,
            DownloadContest.class,
            DownloadContestAPI.class
        },
        mixinStandardHelpOptions = true,
        version = "PolygonContestImporter 1.4.1-SNAPSHOT"
)
public class Main implements Callable<Void> {

    @Spec CommandSpec spec;

    @Override
    public Void call() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Main());
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.setUsageHelpWidth(120);
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
