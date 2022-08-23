package common

import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.readBytes

class Cmd(private val workingDirectory: Path) {
    private fun run(vararg args: String): RunResult {
        val outFile = createTempFile(workingDirectory, "", ".out")
        val errFile = createTempFile(workingDirectory, "", ".err")
        val classPath = System.getProperty("java.class.path")!!
        val builder =
            ProcessBuilder("java", "-cp", classPath, "importer.Main", *args)
                .redirectOutput(outFile.toFile())
                .redirectError(errFile.toFile())
        val process = builder.start()
        val exitCode = process.waitFor()
        val out = outFile.readBytes()
        val err = errFile.readBytes()
        println("out: \"${String(out)}\"")
        println("err: \"${String(err)}\"")
        return RunResult(exitCode, out, err)
    }

    fun succeeds(vararg args: String): RunResult {
        val runResult = run(*args)
        assertEquals(0, runResult.exitCode) {
            "The program expected to succeed, exit code = ${runResult.exitCode}"
        }
        return runResult
    }
}