package pcms2.deployer;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import pcms2.Challenge;
import pcms2.Problem;
import polygon.ProblemDescriptor;
import polygon.ProblemDirectory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

public class Deployer {
    private final static Logger log = LogManager.getLogger(Deployer.class);
    private final File vfsRoot;
    private final File webRoot;

    public Deployer(File vfsRoot, File webRoot) {
        this.vfsRoot = vfsRoot;
        this.webRoot = webRoot;
    }

    public void deployChallengeXML(File src, String challengeId, DeployerConfig config) throws IOException {
        copyToVFS(src, "challenge.xml", challengeId, config);
    }

    public void deploySubmitLst(File src, String challengeId, DeployerConfig config) throws IOException {
        copyToVFS(src, "submit.lst", challengeId, config);
    }

    private void copyToVFS(File contestFile, String destName, String challengeId, DeployerConfig config) throws IOException {
        File vfsEtcDirectory = new File(vfsRoot, "etc/" + challengeId.replace(".", "/"));
        File dest = new File(vfsEtcDirectory, destName);
        log.info("Preparing to copy " + contestFile.getAbsolutePath() + " to " + dest.getAbsolutePath());
        deployFile(contestFile, dest, config::rewriteContestFiles);
    }

    public void copyToVFS(Problem problem, DeployerConfig config) throws IOException {
        File src = problem.getDirectory();
        File dest = resolveProblemVfs(problem.getId());
        log.info("Preparing to copy problem " + problem.getShortName() + " to " + dest.getAbsolutePath());
        deployFile(src, dest, config::rewriteProblemFiles);
    }

    private void deployFile(File src, File dest, Supplier<Boolean> replace) throws IOException {
        if (dest.exists()) {
            log.info(src.getName() + " '" + dest.getAbsolutePath() + "' exists.");
            if (replace.get()) {
                log.info("Updating...");
                forceCopyFileOrDirectory(src, dest);
            } else {
                log.info("Skipping...");
            }
        } else {
            log.info("Copying " + src.getName() + " to '" + dest.getAbsolutePath() + "'.");
            forceCopyFileOrDirectory(src, dest);
        }
    }


    private void forceCopyFileOrDirectory(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            FileUtils.copyDirectory(src, dest);
        } else {
            FileUtils.copyFile(src, dest);
        }
    }

    private File resolveProblemVfs(String problemId) {
        return new File(vfsRoot, "problems/" + problemId.replace(".", "/"));
    }

    public int getVfsProblemRevision(String problemId) throws ParserConfigurationException, SAXException, IOException {
        File probDir = resolveProblemVfs(problemId);
        File problemXMLPolygon = new File(probDir, ProblemDirectory.POLYGON_XML_NAME);
        if (!problemXMLPolygon.exists()) {
            return -1;
        }
        return ProblemDescriptor.parse(problemXMLPolygon).getRevision();
    }

    public void copyToWEB(File srcContestDir, Challenge challenge, DeployerConfig config) throws IOException {
        File src = new File(srcContestDir, "statements/" + challenge.getLanguage() + "/statements.pdf");
        if (!src.exists()) {
            return;
        }
        File dest = new File(webRoot, "statements/" + challenge.getId().replace(".", "/") + "/statements.pdf");
        log.info("Preparing to copy " + challenge.getLanguage() + " statement to " + dest.getAbsolutePath());
        publishFile(src, dest, config);
    }


    public void publishFile(File src, File dest, DeployerConfig config) throws IOException {
        if (config.publishStatement()) {
            log.info("Publishing...");
            forceCopyFileOrDirectory(src, dest);
        } else {
            log.info("Skipping...");
        }
    }
}
