package xmlwrapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

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
}
