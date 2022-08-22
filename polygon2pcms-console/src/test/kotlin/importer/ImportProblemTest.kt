package importer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertNotNull

class ImportProblemTest {

    @Test
    fun testImportExampleAPlusB(@TempDir directory: Path) {
        val fileName = "example-a-plus-b-4.zip"
        val resource = Path.of(assertNotNull(javaClass.getResource(fileName), "Resource $fileName doesn't exist").toURI())
        val archive = resource.copyTo(directory.resolve(resource.fileName))
        val app = Main()
        val cmd = CommandLine(app)
        val exitCode = cmd.execute("problem", "com.demo", archive.absolutePathString())
        assertEquals(0, exitCode) {
            "Exit code is $exitCode"
        }
    }
}
