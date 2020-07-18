package importer;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import pcms2.Challenge;
import pcms2.Problem;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

public class Utils {

    static public void unzip(File zipFile, File probDir) throws ZipException {
        new ZipFile(zipFile).extractAll(probDir.getAbsolutePath());
    }

    static public Stream<String> getLanguagesBySourcePath(String sourcePath, Properties langProperties) {
        String fileName = FilenameUtils.getName(sourcePath);
        String extension = FilenameUtils.getExtension(fileName);
        String languages = langProperties.getProperty(extension);
        return languages == null ? Stream.empty() :
                Arrays.stream(languages.split(",")).map(String::trim).filter(x -> !x.isEmpty());
    }
}
