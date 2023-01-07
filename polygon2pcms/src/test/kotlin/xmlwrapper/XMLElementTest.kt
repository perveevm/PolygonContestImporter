package xmlwrapper

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.xml.sax.SAXException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.stream.Collectors
import javax.xml.parsers.ParserConfigurationException

class XMLElementTest {

    @Test
    fun testSampleContestXML() {
        val xmlString = """
            <contest>
                <names>
                    <name language="russian" value="Some contest"/>
                </names>
                <statements>
                    <statement language="american" type="application/pdf" url="https://polygon.codeforces.com/c/1234/russian/statements.pdf"/>
                </statements>
                <problems languages="russian">
                    <problem index="A" url="https://polygon.codeforces.com/p/someone/problema"/>
                    <problem index="B" url="https://polygon.codeforces.com/p/someone_else/problemb"/>
                    <problem index="C" url="https://polygon.codeforces.com/p/hey/problemc_c"/>
                    <problem index="D" url="https://polygon.codeforces.com/p/zzz/aaa"/>
                </problems>
            </contest>
        """.trimIndent()
        val root = XMLElement.getRoot(xmlString.byteInputStream())
        val names = root.findFirstChild("names")
        assertEquals(1, names.findChildrenStream("name").count()) {
            "number of names is not '1'"
        }
        assertTrue(
            names.findChildrenStream("name").allMatch { it.getAttribute("language") == "russian" }
        ) {
            "name language is not 'russian'"
        }
        val statement = root.findFirstChild("statements").findFirstChild("statement")
        assertEquals("american", statement.getAttribute("language")) {
            "statement language is not 'american'"
        }
        assertEquals("application/pdf", statement.getAttribute("type")) {
            "statement type is not 'application/pdf'"
        }
        assertNotNull(statement.getAttribute("url")) {
            "statement 'url' attribute doesn't exist"
        }
        assertTrue(
            root.findFirstChild("problems").findChildrenStream("problem")
                .allMatch { it.getAttribute("index") != null && it.getAttribute("url") != null }) {
            "some problem has no 'index' or 'url' field"
        }
    }

    private fun getInputStream(xml: String) = ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8))

    private fun getValidInputStream(): InputStream {
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
            <root attribute="value">
                <child attr="val0"></child>
                <child attr="val1"/>
                <child attr="val2"></child>
            </root>
        """
        return getInputStream(xml)
    }

    private fun getInvalidInputStream(): InputStream {
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
            <root attribute="value">
                <child>
            </root>
        """
        return getInputStream(xml)
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testGetRootWithValidXMLFile() {
        val root = XMLElement.getRoot(getValidInputStream())
        assertTrue(root.exists())
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testGetRootWithInvalidXMLFile() {
        assertThrows(SAXException::class.java) {
            XMLElement.getRoot(getInvalidInputStream())
        }
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testGetAttributeWithValidAttributeName() {
        val root = XMLElement.getRoot(getValidInputStream())
        val attribute = root.getAttribute("attribute")
        assertEquals("value", attribute)
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testGetAttributeWithInvalidAttributeName() {
        val root = XMLElement.getRoot(getValidInputStream())
        val attribute = root.getAttribute("invalid")
        assertEquals("", attribute)
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testFindChildrenStreamWithValidTagName() {
        val root = XMLElement.getRoot(getValidInputStream())
        val children = root.findChildrenStream("child").collect(Collectors.toList())
        assertEquals(3, children.size)
        for (i in 0..2) {
            val child = children[i]!!
            assertEquals("child", child.tagName)
            assertEquals("val$i", child.getAttribute("attr"))
        }
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testFindChildrenStreamWithInvalidTagName() {
        val root = XMLElement.getRoot(getValidInputStream())
        val children = root.findChildrenStream("invalid")
        val list = children.collect(Collectors.toList())
        val array = list.toTypedArray()
        assertArrayEquals(emptyArray<XMLElement>(), array)
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testFindChildrenWithValidTagName() {
        val root = XMLElement.getRoot(getValidInputStream())
        val children = root.findChildren("child")
        assertEquals(3, children.size)
        for (i in 0..2) {
            val child = children[i]!!
            assertEquals("child", child.tagName)
            assertEquals("val$i", child.getAttribute("attr"))
        }
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testFindChildrenWithInvalidTagName() {
        val root = XMLElement.getRoot(getValidInputStream())
        val children = root.findChildren("invalid")
        assertArrayEquals(emptyArray<XMLElement>(), children)
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testFindFirstChildWithValidTagName() {
        val root = XMLElement.getRoot(getValidInputStream())
        val child = root.findFirstChild("child")
        assertTrue(child.exists())
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testFindFirstChildWithInvalidTagName() {
        val root = XMLElement.getRoot(getValidInputStream())
        val child = root.findFirstChild("invalid")
        assertFalse(child.exists())
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testGetText() {
        val inputStream = getInputStream(
            """<?xml version="1.0" encoding="UTF-8"?>
            <root>text</root>"""
        )
        val root = XMLElement.getRoot(inputStream)
        assertEquals("text", root.text)
    }

    @Test
    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    fun testExists() {
        val element = XMLElement(null)
        assertFalse(element.exists())

        val root = XMLElement.getRoot(getValidInputStream())
        val child = root.findFirstChild("invalid")
        assertFalse(child.exists())

        val child2 = root.findFirstChild("child")
        assertTrue(child2.exists())
    }

    private fun getUTF8InputStream(): InputStream {
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
            <root>
                <child attr="value">
                    Значение
                </child>
            </root>
        """
        return getInputStream(xml)
    }

    @Test
    fun testUTF8() {
        val root = XMLElement.getRoot(getUTF8InputStream())
        val child = root.findFirstChild("child")
        assertEquals("value", child.getAttribute("attr"))
        assertEquals("Значение", child.text.trim())
    }
}
