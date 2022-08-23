package importer

import common.Cmd
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
    fun testImportExampleAPlusBZip(@TempDir directory: Path) {
        val archive = copyResourceToDirectory("example-a-plus-b-4.zip", directory)
        Cmd(directory).succeeds("problem", "com.demo", archive.absolutePathString())
            .stdout_contains("is not a directory, trying to unzip")
    }

    @Test
    fun testImportExampleAPlusBDir(@TempDir directory: Path) {
        val archive = copyResourceToDirectory("example-a-plus-b-4.zip", directory)
        val dir = directory.resolve("dir")
        Utils.unzip(archive.toFile(), dir.toFile())
        Cmd(directory).succeeds("problem", "com.demo", dir.absolutePathString())
    }
}
