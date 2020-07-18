package pcms2.deployer;

public interface DeployerConfig {
    DeployerConfig ALL = new DeployerConfigImpl(true, true, true);

    boolean rewriteProblemFiles();
    boolean rewriteContestFiles();
    boolean publishStatement();
}
