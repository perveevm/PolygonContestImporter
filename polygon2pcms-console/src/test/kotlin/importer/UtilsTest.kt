package importer
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.Properties
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * This class was generated with the help of an AI assistant: https://chat.openai.com and supervised by a human.
 * It contains test cases for the Utils class.
 */

class UtilsTest {
    private val TEST_ZIP_FILE_NAME = "test.zip"
    private val TEST_PROB_DIR_NAME = "testProbDir"
    private val TEST_SOURCE_PATH = "test/test.ext"
    private val TEST_LANGUAGES = "lang1, lang2"
    private val TEST_NONEXISTENT_ZIP_FILE_NAME = "nonexistent.zip"
    private val TEST_EXISTING_FILE_NAME = "existingFile"
    private val TEST_HELLO_TXT_FILE_NAME = "hello.txt"
    private val TEST_SINGLE_LANGUAGE = "singleLanguage"
    private val TEST_MULTIPLE_LANGUAGES_WITH_SPACES = "  lang1  ,  lang2  ,  lang3  "
    private val TEST_MULTIPLE_LANGUAGES_WITH_EMPTY_LANGUAGES = "lang1,,,lang2, , , lang3,,"

    @TempDir
    lateinit var tempDir: File

    private lateinit var testZipFile: File
    private lateinit var testProbDir: File
    private lateinit var testLangProperties: Properties
    private lateinit var testNonexistentZipFile: File
    private lateinit var testExistingFile: File
    private lateinit var testHelloTxtFile: File

    @BeforeEach
    fun setUp() {
        // Create test zip file
        testZipFile = File(tempDir, TEST_ZIP_FILE_NAME)
        FileUtils.touch(testZipFile)

        // Create test txt file
        testHelloTxtFile = File(tempDir, TEST_HELLO_TXT_FILE_NAME)
        FileUtils.writeLines(testHelloTxtFile, listOf("hello"))

        // Create test existing file
        testExistingFile = File(tempDir, TEST_EXISTING_FILE_NAME)
        FileUtils.touch(testExistingFile)

        // Set up test language properties
        testLangProperties = Properties()
        testLangProperties.setProperty("ext", TEST_LANGUAGES)
        testLangProperties.setProperty("single", TEST_SINGLE_LANGUAGE)
        testLangProperties.setProperty("multiple", TEST_MULTIPLE_LANGUAGES_WITH_SPACES)

        // Create test prob dir
        testProbDir = File(tempDir, TEST_PROB_DIR_NAME)
        testProbDir.mkdir()

        // Create test nonexistent zip file
        testNonexistentZipFile = File(tempDir, TEST_NONEXISTENT_ZIP_FILE_NAME)
    }

    @AfterEach
    fun tearDown() {
        // Delete test zip file
        testZipFile.delete()

        // Delete test prob dir
        FileUtils.deleteDirectory(testProbDir)

        // Delete test nonexistent zip file
        testNonexistentZipFile.delete()

        // Delete test existing file
        testExistingFile.delete()

        // Delete test hello text file
        testHelloTxtFile.delete()
    }

    @Test
    fun testUnzip() {
        // Add a file to the test zip file
        ZipOutputStream(FileOutputStream(testZipFile)).use { out ->
            out.putNextEntry(ZipEntry("test.txt"))
            out.write("test".toByteArray())
            out.closeEntry()
        }

        // Unzip test zip file
        Utils.unzip(testZipFile, testProbDir)

        // Assert that the test prob dir contains the test zip file's contents
        val testFile = testProbDir.listFiles()!!.first { file -> file.name == "test.txt" }
        assertTrue(testFile.exists())

        // Assert that the contents of "test.txt" are correct
        val contents = String(Files.readAllBytes(testFile.toPath()))
        assertEquals("test", contents)
    }


    @Test
    fun testUnzipNonexistentFile() {
        // Assert that an exception is thrown when trying to unzip a nonexistent file
        assertThrows(IOException::class.java) { Utils.unzip(testNonexistentZipFile, testProbDir) }
    }

    @Test
    fun testUnzipHelloFile() {
        // Assert that an exception is thrown when trying to unzip a non-zip file
        assertThrows(IOException::class.java) { Utils.unzip(testHelloTxtFile, testProbDir) }
    }

    @Test
    fun testGetLanguagesBySourcePath() {
        // Set up test language properties
        testLangProperties.setProperty("ext", TEST_LANGUAGES)

        // Get languages for test source file
        val languages = Utils.getLanguagesBySourcePath(TEST_SOURCE_PATH, testLangProperties)
            .collect(Collectors.toList())

        // Assert that the correct languages are returned
        assertEquals(2, languages.size)
        assertTrue(languages.contains("lang1"))
        assertTrue(languages.contains("lang2"))
    }

    @Test
    fun testGetLanguagesBySourcePathSingleLanguage() {
        // Set up test language properties with a single language
        testLangProperties.setProperty("single", TEST_SINGLE_LANGUAGE)

        // Get languages for test source file with the "single" extension
        val languages = Utils.getLanguagesBySourcePath("test/test.single", testLangProperties)
            .collect(Collectors.toList())

        // Assert that the correct language is returned
        assertEquals(1, languages.size)
        assertTrue(languages.contains(TEST_SINGLE_LANGUAGE))
    }

    @Test
    fun testGetLanguagesBySourcePathNonexistentExtension() {
        // Get languages for test source file with a nonexistent extension
        val languages = Utils.getLanguagesBySourcePath("test/test.nonexistent", testLangProperties)
            .collect(Collectors.toList())

        // Assert that no languages are returned
        assertTrue(languages.isEmpty())
    }

    private fun testGetLanguagesBySourcePathMultipleLanguages(languagesProperty: String) {
        // Set up test language properties with multiple languages
        testLangProperties.setProperty("multiple", languagesProperty)

        // Get languages for test source file with the "multiple" extension
        val languages = Utils.getLanguagesBySourcePath("test/test.multiple", testLangProperties)
            .collect(Collectors.toList())

        // Assert that the correct languages are returned
        assertEquals(3, languages.size)
        assertTrue(languages.contains("lang1"))
        assertTrue(languages.contains("lang2"))
        assertTrue(languages.contains("lang3"))
    }

    @Test
    fun testGetLanguagesBySourcePathMultipleLanguagesWithSpaces() {
        testGetLanguagesBySourcePathMultipleLanguages(TEST_MULTIPLE_LANGUAGES_WITH_SPACES)
    }

    @Test
    fun testGetLanguagesBySourcePathMultipleLanguagesWithEmptyLanguages() {
        testGetLanguagesBySourcePathMultipleLanguages(TEST_MULTIPLE_LANGUAGES_WITH_EMPTY_LANGUAGES)
    }
}
