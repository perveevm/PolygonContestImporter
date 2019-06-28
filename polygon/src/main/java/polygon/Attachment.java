package polygon;

import org.w3c.dom.Element;

public class Attachment {
    String path;
    String type;

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public static Attachment parse(Element el) {
        Attachment attachment = new Attachment();
        attachment.path = el.getAttribute("path");
        attachment.type = el.getAttribute("type");
        return attachment;
    }
}
