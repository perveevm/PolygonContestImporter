package importer;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PackageDownloader {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final String username;
    private final String password;

    public PackageDownloader(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean downloadPackage(String url, String type, File zipFile) throws IOException {
        String strType = type == null ? "standard" : "full";
        HttpPost postRequest = getPostRequest(url, type);
        int statusCode = downloadFile(postRequest, zipFile);
        if (statusCode != 200) {
            System.err.println("Failed to download " + strType + " package: " + statusCode);
        }
        return statusCode == 200;
    }

    public boolean downloadByURL(String url, File file) throws IOException {
        HttpPost postRequest = getPostRequest(url, null);
        int statusCode = downloadFile(postRequest, file);
        if (statusCode != 200) {
            System.err.println("Couldn't download " + url);
        }
        return statusCode == 200;
    }

    private int downloadFile(HttpPost postRequest, File destFile) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                return statusCode;
            }
            HttpEntity entity = response.getEntity();
            long fileSize = entity.getContentLength();
            System.out.println("Downloading file " + postRequest.getURI().toString() + " (" + fileSize + " bytes) -> " + destFile.getAbsolutePath());
            FileUtils.copyInputStreamToFile(entity.getContent(), destFile);
            if (fileSize >= 0 && fileSize != destFile.length()) {
                throw new AssertionError("Couldn't download file"); // TODO
            } else {
                System.out.println("Downloaded " + destFile.length() + " bytes successfully");
            }
            return statusCode;
        }
    }

    public boolean downloadContestXml(String uid, File contestXMLFile) throws IOException {
        return downloadFile(getPostRequest("https://polygon.codeforces.com/c/" + uid + "/contest.xml", null), contestXMLFile) == 200;
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
}
