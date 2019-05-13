package polygon;

import org.w3c.dom.Element;

public class Interactor {
    String binaryPath;
    String binaryType;
    String sourcePath;
    String sourceType;

    public static Interactor parse(Element interactorNode) {
        if (interactorNode == null) {
            return null;
        }

        Element el = (Element) interactorNode.getElementsByTagName("binary").item(0);
        Interactor interactor = new Interactor();
        if (el != null) {
            interactor.binaryPath = el.getAttribute("path");
            interactor.binaryType = el.getAttribute("type");
        }

        el = (Element) interactorNode.getElementsByTagName("source").item(0);
        interactor.sourcePath = el.getAttribute("path");
        interactor.sourceType = el.getAttribute("type");
        return interactor;
    }

    public String getBinaryPath() {
        return binaryPath;
    }

    public String getBinaryType() {
        return binaryType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getSourceType() {
        return sourceType;
    }
}
