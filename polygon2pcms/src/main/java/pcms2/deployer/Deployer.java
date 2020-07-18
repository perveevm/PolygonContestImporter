package pcms2.deployer;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;
import pcms2.Challenge;
import pcms2.Problem;
import polygon.ProblemDescriptor;
import polygon.ProblemDirectory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Supplier;

public class Deployer {
    private File vfsRoot;
    private File webRoot;
    private PrintStream logger;

    public Deployer(File vfsRoot, File webRoot, PrintStream logger) {
        this.vfsRoot = vfsRoot;
        this.webRoot = webRoot;
        this.logger = logger;
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
        logger.println("Preparing to copy " + contestFile.getAbsolutePath() + " to " + dest.getAbsolutePath());
        deployFile(contestFile, dest, config::rewriteContestFiles);
    }

    public void copyToVFS(Problem problem, DeployerConfig config) throws IOException {
        File src = problem.getDirectory();
        File dest = resolveProblemVfs(problem.getId());
        logger.println("Preparing to copy problem " + problem.getShortName() + " to " + dest.getAbsolutePath());
        deployFile(src, dest, config::rewriteProblemFiles);
    }

    private void deployFile(File src, File dest, Supplier<Boolean> replace) throws IOException {
        if (dest.exists()) {
            logger.println(src.getName() + " '" + dest.getAbsolutePath() + "' exists.");
            if (replace.get()) {
                logger.println("Updating...");
                forceCopyFileOrDirectory(src, dest);
            } else {
                logger.println("Skipping...");
            }
        } else {
            logger.println("Copying " + src.getName() + " to '" + dest.getAbsolutePath() + "'.");
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
        logger.println("Preparing to copy " + challenge.getLanguage() + " statement to " + dest.getAbsolutePath());
        publishFile(src, dest, config);
    }


    public void publishFile(File src, File dest, DeployerConfig config) throws IOException {
        if (config.publishStatement()) {
            logger.println("Publishing...");
            forceCopyFileOrDirectory(src, dest);
        } else {
            logger.println("Skipping...");
        }
    }
}
