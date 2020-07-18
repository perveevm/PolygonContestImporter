package pcms2.deployer;

public class DeployerConfigImpl implements DeployerConfig {
    boolean rewriteProblemFiles;
    boolean rewriteContestFiles;
    boolean publishStatement;

    public DeployerConfigImpl(boolean rewriteProblemFiles, boolean rewriteContestFiles, boolean publishStatement) {
        this.rewriteProblemFiles = rewriteProblemFiles;
        this.rewriteContestFiles = rewriteContestFiles;
        this.publishStatement = publishStatement;
    }

    @Override
    public boolean rewriteProblemFiles() {
        return rewriteProblemFiles;
    }

    @Override
    public boolean rewriteContestFiles() {
        return rewriteContestFiles;
    }

    @Override
    public boolean publishStatement() {
        return publishStatement;
    }
}
