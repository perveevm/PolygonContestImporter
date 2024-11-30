package polygon.download;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarConsumer;
import me.tongfei.progressbar.ProgressBarStyle;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;
import polygon.ContestDescriptor;
import polygon.ContestXML;
import polygon.ProblemDescriptor;
import ru.perveevm.polygon.api.PolygonSession;
import ru.perveevm.polygon.api.PolygonSessionBuilder;
import ru.perveevm.polygon.api.entities.Problem;
import ru.perveevm.polygon.api.entities.ProblemPackage;
import ru.perveevm.polygon.api.entities.enums.PackageState;
import ru.perveevm.polygon.exceptions.api.PolygonSessionException;
import tempfilemanager.TemporaryFileManager;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class PackageDownloader {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private PolygonSession session;

    private final String username;
    private final String password;
    private final String apiKey;
    private final String apiSecret;
    private final PrintStream logger;
    private final ProgressBarConsumer pbbConsumer;

    public PackageDownloader(String username, String password) {
        this(username, password, null, null, System.out, null);
    }

    public PackageDownloader(String username, String password, String apiKey, String apiSecret) {
        this(username, password, apiKey, apiSecret, System.out, null);
    }

    public PackageDownloader(String username, String password, PrintStream logger) {
        this(username, password, null, null, logger, new ProgressBarConsumer() {
            private String lastLine = null;

            @Override
            public int getMaxRenderedLength() {
                return 100;
            }

            @Override
            public void accept(String rendered) {
                lastLine = rendered;
            }

            @Override
            public void close() {
                logger.println(lastLine);
            }
        });
    }

    private PackageDownloader(String username, String password, String apiKey, String apiSecret, PrintStream logger, ProgressBarConsumer consumer) {
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.logger = logger;
        this.pbbConsumer = consumer;

        if (this.apiKey != null && this.apiSecret != null) {
            this.session = PolygonSessionBuilder.defaultRetryPolygonSession(this.apiKey, this.apiSecret);
        }
    }

    public boolean downloadPackage(String url, String type, File zipFile) throws IOException {
        String strType = type == null ? "standard" : "full";
        HttpPost postRequest = getPostRequest(url, type);
        int statusCode = downloadFile(postRequest, zipFile);
        if (statusCode != 200) {
            logger.println("[WARN] Failed to download " + strType + " package: " + statusCode);
        }
        return statusCode == 200;
    }

    public boolean downloadPackageAPI(Integer problemId, Integer packageId, String type, File zipFile) {
        String strType = type == null ? "standard" : "windows";
        try {
            session.problemPackage(problemId, packageId, strType, zipFile);
            return true;
        } catch (PolygonSessionException e) {
            logger.println("[WARN] Failed to download " + strType + " package: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Problem> getContestProblems(Integer contestId) throws PolygonSessionException {
        return session.contestProblems(contestId);
    }

    public boolean downloadByURL(String url, File file) throws IOException {
        HttpPost postRequest = getPostRequest(url, null);
        int statusCode = downloadFile(postRequest, file);
        if (statusCode != 200) {
            logger.println("[WARN] Couldn't download " + url);
        }
        return statusCode == 200;
    }

    private int download(HttpPost postRequest, OutputStream dest, String fileName) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                return statusCode;
            }
            HttpEntity entity = response.getEntity();
            long fileSize = entity.getContentLength();
            logger.println("Downloading file " + fileName + " from " + postRequest.getUri().toString() + " (" + fileSize + " bytes)");
            ProgressBarBuilder pbb = new ProgressBarBuilder()
                    .setInitialMax(fileSize)
                    .showSpeed()
                    .setTaskName(fileName)
                    .setStyle(ProgressBarStyle.ASCII)
                    .setUpdateIntervalMillis(100)
                    .setConsumer(pbbConsumer);
            if (fileSize > 10 * (1 << 20)) {
                pbb = pbb.setUnit("MiB", 1L << 20);
            } else if (fileSize > 10 * (1 << 10)) {
                pbb = pbb.setUnit("KiB", 1L << 10);
            } else {
                pbb = pbb.setUnit("B", 1);
            }
            try (InputStream readFrom = ProgressBar.wrap(entity.getContent(), pbb)) {
                int copied = IOUtils.copy(readFrom, dest);
                if (fileSize >= 0 && copied >= 0 && fileSize != copied) {
                    throw new AssertionError("Couldn't download file"); // TODO
                } else {
                    logger.println("Downloaded " + copied + " bytes successfully");
                }
            }
            return statusCode;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private int downloadFile(HttpPost postRequest, File destFile) throws IOException {
        try (OutputStream dest = Files.newOutputStream(destFile.toPath())) {
            int statusCode = download(postRequest, dest, destFile.getName());
            if (statusCode == 200) {
                logger.println("Downloaded file saved to -> " + destFile.getAbsolutePath());
            }
            return statusCode;
        }
    }

    public boolean downloadContestXml(String uid, File contestXMLFile) throws IOException {
        return downloadFile(getPostRequest("https://polygon.codeforces.com/c/" + uid + "/contest.xml", null), contestXMLFile) == 200;
    }

    public byte[] downloadToByteArray(HttpPost postRequest, String fileName) throws IOException {
        try (ByteArrayOutputStream downloadedFile = new ByteArrayOutputStream()) {
            int statusCode = download(postRequest, downloadedFile, fileName);
            if (statusCode != 200) {
                return null;
            }
            return downloadedFile.toByteArray();
        }
    }

    public ContestXML downloadContestXML(String uid) throws IOException, ParserConfigurationException, SAXException {
        byte[] contestXmlFile = downloadContestXMLtoBytes(uid);
        try (ByteArrayInputStream toParse = new ByteArrayInputStream(contestXmlFile)) {
            return ContestXML.parse(toParse);
        }
    }

    public ProblemDescriptor downloadProblemDescriptor(String link) throws IOException, ParserConfigurationException, SAXException {
        HttpPost postRequest = getPostRequest(link + "/problem.xml", null);
        byte[] problemXMLFile = downloadToByteArray(postRequest, "problem.xml");
        try (ByteArrayInputStream toParse = new ByteArrayInputStream(problemXMLFile)) {
            return ProblemDescriptor.parse(toParse);
        }
    }

    private byte[] downloadContestXMLtoBytes(String uid) throws IOException {
        HttpPost postRequest = getPostRequest("https://polygon.codeforces.com/c/" + uid + "/contest.xml", null);
        byte[] contestXmlFile = downloadToByteArray(postRequest, "contest.xml");
        if (contestXmlFile == null) {
            throw new AssertionError("Couldn't download contest.xml for uid = " + uid);
        }
        return contestXmlFile;
    }

    public ContestDescriptor downloadContestDescriptor(String uid) throws ParserConfigurationException, SAXException, IOException {
        try (ByteArrayInputStream contestXMLStream = new ByteArrayInputStream(downloadContestXMLtoBytes(uid))) {
            return new ContestDescriptor(contestXMLStream, contestXML ->
                contestXML.getProblemLinks().entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            try {
                                return downloadProblemDescriptor(entry.getValue());
                            } catch (IOException | SAXException | ParserConfigurationException e) {
                                throw new RuntimeException(e);
                            }
                        }, (x, y) -> y, TreeMap::new))
            );
        }
    }

    public boolean downloadProblemXml(String url, File problemXmlFile) throws IOException {
        return downloadFile(getPostRequest(url + "/problem.xml", null), problemXmlFile) == 200;
    }

    private HttpPost getPostRequest(String url, String type) throws UnsupportedEncodingException {
        HttpPost postRequest = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("login", username));
        params.add(new BasicNameValuePair("password", password));
        if (type != null) {
            params.add(new BasicNameValuePair("type", type));
        }
        postRequest.setEntity(new UrlEncodedFormEntity(params));
        return postRequest;
    }

    public PolygonPackageType downloadProblemDirectory(String polygonUrl, File probDir, TemporaryFileManager fileManager) throws IOException {
        try {
            File zipFile = fileManager.createTemporaryFile("__archive", ".zip");
            PolygonPackageType fullPackage = downloadProblemPackage(polygonUrl, zipFile);
            try (ZipFile zip = new ZipFile(zipFile)) {
                zip.extractAll(probDir.getAbsolutePath());
            }
            return fullPackage;
        } catch (ZipException e) {
            throw new AssertionError(e);
        }
    }

    public PolygonPackageType downloadProblemDirectoryAPI(Integer problemId, Integer latestRevision, File probDir, TemporaryFileManager fileManager) throws IOException {
        Integer packageId = null;
        try {
            ProblemPackage[] packages = session.problemPackages(problemId);
            packageId = Arrays.stream(packages)
                    .filter(p -> p.getState() == PackageState.READY)
                    .map(ProblemPackage::getId)
                    .max(Integer::compareTo)
                    .orElseThrow(() -> new PolygonSessionException("There are not ready packages for problem"));
        } catch (PolygonSessionException e) {
            logger.println("[WARN] Unable to fetch problem packages list for problem " + problemId);
            throw new AssertionError(e);
        }
        logger.println("Trying to download package " + packageId + " of problem " + problemId);
        try {
            File zipFile = fileManager.createTemporaryFile("__archive", ".zip");
            PolygonPackageType fullPackage = downloadProblemPackageAPI(problemId, packageId, zipFile);
            try (ZipFile zip = new ZipFile(zipFile)) {
                zip.extractAll(probDir.getAbsolutePath());
            }
            return fullPackage;
        } catch (ZipException e) {
            throw new AssertionError(e);
        }
    }

    private PolygonPackageType downloadProblemPackage(String polygonUrl, File zipFile) throws IOException {
        if (username == null || password == null) {
            throw new AssertionError("Polygon username or password is not set");
        }
        if (downloadPackage(polygonUrl, "windows", zipFile)) {
            return PolygonPackageType.WINDOWS;
        }
        if (downloadPackage(polygonUrl, null, zipFile)) {
            return PolygonPackageType.STANDARD;
        }
        throw new AssertionError("Couldn't download any package");
    }

    private PolygonPackageType downloadProblemPackageAPI(Integer problemId, Integer packageId, File zipFile) {
        if (apiKey == null || apiSecret == null) {
            throw new AssertionError("Polygon API key or API secret is not set");
        }
        if (downloadPackageAPI(problemId, packageId, "windows", zipFile)) {
            return PolygonPackageType.WINDOWS;
        }
        if (downloadPackageAPI(problemId, packageId, null, zipFile)) {
            return PolygonPackageType.STANDARD;
        }
        throw new AssertionError("Couldn't download any package");
    }
}
