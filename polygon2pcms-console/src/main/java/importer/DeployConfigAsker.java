package importer;

import pcms2.deployer.DeployerConfig;

public class DeployConfigAsker implements DeployerConfig {
    private Asker asker;

    public DeployConfigAsker(Asker asker) {
        this.asker = asker;
    }

    @Override
    public boolean rewriteProblemFiles() {
        return asker.askForUpdate("Update problem directory?");
    }

    @Override
    public boolean rewriteContestFiles() {
        return asker.askForUpdate("File exists, update file?");
    }

    @Override
    public boolean publishStatement() {
        return asker.askForUpdate("Publish statement?");
    }
}
