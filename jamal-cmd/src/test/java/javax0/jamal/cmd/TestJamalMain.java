package javax0.jamal.cmd;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
    public void testHelpScreenIsDisplayed() throws BadSyntax {
        Assertions.assertEquals("Usage:", jamal("-h").substring(0, 6));
    }

    @Test
    @DisplayName("Command line converts a single file")
    public void testConvertSingleFile() throws Exception {
        final var out = jamal("-f","src/test/resources/test.jam","target/test-classes/test","-v");
        Assertions.assertTrue(Pattern.compile("Jamal .*/jamal-cmd/src/test/resources/test.jam -> .*/jamal-cmd/target/test-classes/test").matcher(out).find(),
            () -> "out: "+out
        );
        TestThat.theInput("{@include res:test}").results("1");
    }

}
