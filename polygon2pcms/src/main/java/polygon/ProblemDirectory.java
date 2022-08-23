package polygon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.TreeMap;

public class ProblemDirectory extends ProblemDescriptor {
    private final static Logger log = LogManager.getLogger(ProblemDirectory.class);
    public static final String POLYGON_XML_NAME = "problem.xml.polygon";
    protected File directory;
    protected File groupsTxtFile;

    static private File findProblemXML(String path) throws FileNotFoundException {
        File directory = new File(path);
        if (!directory.exists()) {
            throw new FileNotFoundException("ERROR: Couldn't find directory '" + path + "'");
        }
        if (!directory.isDirectory()) {
            throw new AssertionError("ERROR: This should be directory '" + path + "'");
        }
        File xmlFile = new File(directory, POLYGON_XML_NAME);
        if (!xmlFile.exists()) {
            if (!(new File(directory, "problem.xml")).renameTo(xmlFile)) {
                throw new FileNotFoundException("ERROR: problem.xml not found in '" + directory + "'!");
            }
        }
        return xmlFile;
    }

    private ProblemDirectory(String path) {
        directory = new File(path);
        groupsTxtFile = new File(path + "/files/groups.txt");
        if (!groupsTxtFile.exists()) {
            groupsTxtFile = null;
        }
    }

    public static ProblemDirectory parse(String path) throws ParserConfigurationException, IOException, SAXException {
        ProblemDirectory problem = new ProblemDirectory(path);
        problem.parseDescriptor(findProblemXML(path));
        problem.parseGroupsTxt();
        return problem;
    }

    public void parseGroupsTxt() throws IOException {
        if (groupsTxtFile == null) return;
        if (!testsets.containsKey("tests")) return;

        try (BufferedReader br = new BufferedReader(new FileReader(groupsTxtFile))) {
            Testset testset = testsets.get("tests");
            if (testset.groups == null) {
                testset.groups = new TreeMap<>();
            }
            boolean hasGroups = testset.groups.size() > 0;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] group_params = line.split("(\t;)|(\t)|(;)");
                TreeMap<String, String> group_par = new TreeMap<>();
                for (int ig = 0; ig < group_params.length; ig++) {
                    String[] kv = getKeyAndValue(group_params[ig]);
                    group_par.put(kv[0], kv[1]);
                }

                if (!group_par.containsKey("group")) {
                    log.warn("Group id was not found! " +
                            "Group parameters:'" + Arrays.toString(group_params) + "'. ");
                    continue;
                }

                Group group = testset.groups.get(group_par.get("group"));
                if (group == null) {
                    if (hasGroups) {
                        log.warn("It seems that there are no tests in group '{}', skipping", group_par.get("group"));
                        continue;
                    }
                    group = new Group();
                    group.name = group_par.get("group");
                    testset.groups.put(group_par.get("group"), group);
                }
                group.parameters = group_par;
            }
        }
    }

    private static String[] getKeyAndValue(String s) {
        //key="value"
        int j = s.indexOf('=');
        String[] ss = new String[2];
        ss[0] = s.substring(0, j).trim();
        ss[1] = s.substring(j + 1).trim();
        ss[1] = ss[1].substring(1, ss[1].length() - 1);
        ss[1] = ss[1].replaceAll("<", "&lt;");
        ss[1] = ss[1].replaceAll(">", "&gt;");

        return ss;
    }

    public File getDirectory() {
        return directory;
    }
}
