package javax0.jamal.cmd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class TestJamalMain {

    private PrintStream saveOut;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setStandardOutput() {
        saveOut = System.out;
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @AfterEach
    void resetStandardOutput() {
        System.setOut(saveOut);
        testOut = null;
    }

    private String jamal(final String... args) {
        JamalMain.main(args);
        return testOut.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Command line displays help screen")
    public void testHelpScreenIsDisplayed() {
        Assertions.assertEquals("Usage:", jamal("-h").substring(0, 6));
    }

    @Test
    @DisplayName("Command line displays version")
    public void testVersionScreenIsDisplayed() {
        Assertions.assertEquals("Jamal Version ", jamal("-vers").substring(0, 14));
    }

    @Test
    @DisplayName("Command line converts a single file with verbose output")
    public void testConvertSingleFile() {
        final var out = jamal("src/test/resources/test.jam", "target/test-classes/test", "--verbose").replaceAll("\\\\", "/");
        Assertions.assertAll(
                () -> Assertions.assertTrue(Pattern.compile("Jamal .*/jamal-cmd/src/test/resources/test.jam -> .*/jamal-cmd/target/test-classes/test").matcher(out).find(),
                        () -> "out: " + out),
                () -> Assertions.assertTrue(Files.exists(Paths.get("target/test-classes/test"))),
                () -> Assertions.assertEquals("1", Files.readString(Paths.get("target/test-classes/test")))
        );
    }

    @Test
    @DisplayName("Command line converts multiple files with verbose output")
    public void testConvertMultipleFiles() {
        final var out = jamal("-include=\\..*jam$", "-source=src/test/resources/multiple_files", "-target=target/test-classes/", "--verbose")
                .replaceAll("\\\\", "/").replaceAll("\r","");
        Assertions.assertAll(
                () -> Assertions.assertTrue(Pattern.compile(
                                "(:?Jamal .*?/jamal-cmd/src/test/resources/multiple_files/test\\d.jam -> .*?/jamal-cmd/target/test-classes/test\\d\n){3}"
                        ).matcher(out).find(),
                        () -> "out: " + out
                ),
                () -> Assertions.assertTrue(Files.exists(Paths.get("target/test-classes/test1"))),
                () -> Assertions.assertTrue(Files.exists(Paths.get("target/test-classes/test2"))),
                () -> Assertions.assertTrue(Files.exists(Paths.get("target/test-classes/test3")))
        );
    }
    @Test
    @DisplayName("Command line converts a single docx file")
    public void testConvertSingleDocFile() {
        final var out = jamal("src/test/resources/DOCX/test1.docx", "target/test-classes/test1.docx", "--docx").replaceAll("\\\\", "/");
        Assertions.assertAll(
                () -> Assertions.assertTrue(Files.exists(Paths.get("target/test-classes/test1.docx")))
        );
    }
}
