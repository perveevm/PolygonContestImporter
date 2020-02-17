package polygon;

import xmlwrapper.XMLElement;

public class Attachment {
    String path;
    String type;

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public static Attachment parse(XMLElement el) {
        Attachment attachment = new Attachment();
        attachment.path = el.getAttribute("path");
        attachment.type = el.getAttribute("type");
        return attachment;
    }
}
