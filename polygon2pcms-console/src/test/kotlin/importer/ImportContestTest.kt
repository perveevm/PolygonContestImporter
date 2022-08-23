package importer

import common.Cmd
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class ImportContestTest {

    @Test
    fun testImportIOIContestZip(@TempDir directory: Path) {
        val zipFile = copyResourceToDirectory("contest-12400.zip", directory)
        Cmd(directory).succeeds("contest", "com.demo", "ioi", zipFile.absolutePathString())
            .stdout_contains("is not a directory, trying to unzip")
    }

    @Test
    fun testImportIOIContestDir(@TempDir tmpdir: Path) {
        val archive = copyResourceToDirectory("contest-12400.zip", tmpdir)
        val dir = tmpdir.resolve("contest")
        Utils.unzip(archive.toFile(), dir.toFile())
        Cmd(tmpdir).succeeds("contest", "com.demo", "ioi", dir.absolutePathString())
    }
}
