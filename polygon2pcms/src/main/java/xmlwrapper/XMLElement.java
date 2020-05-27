package xmlwrapper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class XMLElement {
    private final Element element;

    public static XMLElement getRoot(File xmlFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        return new XMLElement(document.getDocumentElement());
    }

    public XMLElement(Element element) {
        this.element = element;
    }

    public String getAttribute(String name) {
        return element.getAttribute(name);
    }

    private Stream<Element> childrenStream() {
        if (element == null) {
            return Stream.of();
        }
        NodeList list = element.getChildNodes();
        return IntStream.range(0, list.getLength())
                .mapToObj(list::item)
                .filter(x -> x.getNodeType() == Node.ELEMENT_NODE)
                .map(x -> (Element) x);
    }

    public Stream<XMLElement> findChildrenStream(String tagName) {
        return childrenStream()
                .filter(x -> x.getNodeName().equals(tagName))
                .map(XMLElement::new);
    }

    public XMLElement[] findChildren(String tagName) {
        return findChildrenStream(tagName).toArray(XMLElement[]::new);
    }

    public XMLElement findFirstChild(String tagName) {
        return findChildrenStream(tagName).findFirst().orElse(new XMLElement(null));
    }

    public String getText() {
        return element.getTextContent();
    }

    public boolean exists() {
        return element != null;
    }
}
