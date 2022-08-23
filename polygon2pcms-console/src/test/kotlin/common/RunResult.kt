package common

import java.nio.charset.Charset
import org.junit.jupiter.api.Assertions.assertTrue

class RunResult(val exitCode: Int, val out: ByteArray, err: ByteArray) {
    fun stdout_contains(text: String): RunResult {
        val stdoutStr = String(out, Charset.forName("UTF-8"))
        assertTrue(stdoutStr.contains(text)) {
            "stdout = \"$stdoutStr\" doesn't contain \"$text\""
        }
        return this
    }
}