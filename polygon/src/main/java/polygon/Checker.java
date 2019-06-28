package polygon;

import org.w3c.dom.Element;

public class Checker {
    String type;
    String binaryPath;
    String binaryType;

    public static Checker parse(Element el) {
        Checker checker = new Checker();
        checker.type = el.getAttribute("type");
        el = (Element) el.getElementsByTagName("binary").item(0);
        checker.binaryType = el.getAttribute("type");
        checker.binaryPath = el.getAttribute("path");
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
}
