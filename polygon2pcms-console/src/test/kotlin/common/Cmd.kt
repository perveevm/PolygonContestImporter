package common

import importer.Main
import org.junit.jupiter.api.Assertions.assertEquals
import picocli.CommandLine
import java.nio.file.Path

class Cmd(private val workingDirectory: Path) {
    private fun run(vararg args: String): RunResult {
        val exitCode = CommandLine(Main()).execute(*args)
        return RunResult(exitCode)
    }

    fun succeeds(vararg args: String): RunResult {
        val runResult = run(*args)
        assertEquals(0, runResult.exitCode) {
            "The program expected to succeed, exit code = ${runResult.exitCode}"
        }
        return runResult
    }
}
