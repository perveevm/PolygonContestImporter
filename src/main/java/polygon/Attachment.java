package polygon;

import xmlwrapper.XMLElement;

public class Attachment {
    private final String path;

    public Attachment(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static Attachment parse(XMLElement el) {
        return new Attachment(el.getAttribute("path"));
    }
}
