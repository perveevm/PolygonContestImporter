package polygon;

import xmlwrapper.XMLElement;

public class Interactor {
    String binaryPath;
    String binaryType;
    String sourcePath;
    String sourceType;

    public static Interactor parse(XMLElement interactorElement) {
        if (!interactorElement.exists()) {
            return null;
        }

        XMLElement binaryElement = interactorElement.findFirstChild("binary");
        Interactor interactor = new Interactor();
        if (binaryElement.exists()) {
            interactor.binaryPath = binaryElement.getAttribute("path");
            interactor.binaryType = binaryElement.getAttribute("type");
        }

        XMLElement sourceElement = interactorElement.findFirstChild("source");
        interactor.sourcePath = sourceElement.getAttribute("path");
        interactor.sourceType = sourceElement.getAttribute("type");
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
