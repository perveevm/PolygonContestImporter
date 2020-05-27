package polygon;

import xmlwrapper.XMLElement;

public class Checker {
    String type;
    private String source;
    private String sourceType;
    String binaryPath;
    String binaryType;

    public static Checker parse(XMLElement checkerElement) {
        Checker checker = new Checker();
        checker.type = checkerElement.getAttribute("type");
        XMLElement sourceElement = checkerElement.findFirstChild("source");
        checker.source = sourceElement.getAttribute("path");
        checker.sourceType = sourceElement.getAttribute("type");
        XMLElement binaryElement = checkerElement.findFirstChild("binary");
        checker.binaryType = binaryElement.getAttribute("type");
        checker.binaryPath = binaryElement.getAttribute("path");
        return checker;
    }

    public String getType() {
        return type;
    }

    public String getBinaryPath() {
        return binaryPath;
    }

    public String getBinaryType() {
        return binaryType;
    }

    public String getSource() {
        return source;
    }

    public String getSourceType() {
        return sourceType;
    }
}
