package pcms2;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import polygon.SolutionResource;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Ilshat on 7/27/2016.
 */
public class Attachment {
    private final String href;
    private final String languageId;

    private Attachment(String href, String languageId) {
        this.href = href;
        this.languageId = languageId;
    }

    public void print(PrintWriter pw, String tabs){
        pw.println(tabs + "<attachment language-id=\"" + languageId + "\" href=\"" + href + "\"/>");
    }

    public static List<Attachment> parse(List<SolutionResource> resources, Properties languageProps) {
        return resources.stream().flatMap(resource -> {
            String ext = resource.getForTypes().split("\\.", 2)[0];
            String languages = languageProps.getProperty(ext);
            return languages == null ? Stream.empty() :
                    Arrays.stream(languages.split(","))
                            .map(String::trim)
                            .map(lang -> new Attachment(resource.getPath(), lang));
        }).collect(Collectors.toList());
    }

    public static List<Attachment> parse(List<polygon.Attachment> attachments, Properties languagesProps, String shortName) {
        List<Attachment> result = new ArrayList<>();
        Logger log = LogManager.getLogger();
        for (polygon.Attachment attachment : attachments) {
            String atpath = attachment.getPath();
            String atname = FilenameUtils.getName(atpath);
            String ext = FilenameUtils.getExtension(atname);
            String fname = FilenameUtils.getBaseName(atname);
            log.debug("DEBUG: File name is '\" + fname + \"'");
            if (fname.equals("Solver") || (fname.equals(shortName) && !ext.equals("h"))) {
                log.info("Skipping solution stub '" + fname + "." + ext + "'");
                continue;
            }

            if (languagesProps.getProperty(ext) != null) {
                String[] langs = languagesProps.getProperty(ext).split(",");

                for (String lang : langs) {
                    result.add(new Attachment(atpath, lang.trim()));
                }
            }
        }
        return result;
    }
}
