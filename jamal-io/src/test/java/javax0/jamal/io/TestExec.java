package javax0.jamal.io;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Testing external process execution is system dependent and makes not much sense to do uint testing.
 * These tests are integration tests that are executed only occasionally.
 */
public class TestExec {

    @Test
    @DisplayName("Executing 'java -version' will print out the Java version to the console.")
    void test() throws Exception {
        System.setProperty("exec", "java");
        TestThat.theInput("" +
                "{@io:exec command=EXEC argument=\"-version\"}"
        ).results("");
    }

    @Test
    @DisplayName("Executing 'echo hello' print out 'hello' to file")
    void testOutputToFile() throws Exception {
        System.setProperty("exec", "echo");
        Files.deleteIfExists(Paths.get("target/hello.txt"));
        TestThat.theInput("" +
                        "{@io:exec command=EXEC argument=\"hello\" output=\"target/hello.txt\"}"
                ).atPosition(".", 1, 1)
                .results("");
        Assertions.assertEquals("hello\n", Files.readString(Paths.get("target/hello.txt")));
    }

    @Test
    @DisplayName("Executing 'cat' print out 'macro text' as the macro result")
    void testOutputAndInput() throws Exception {
        System.setProperty("exec", "cat");
        Files.deleteIfExists(Paths.get("target/catoutput.txt"));
        TestThat.theInput("" +
                        "{@io:exec command=EXEC output=\"target/catoutput.txt\"\n" +
                        "hello, this is the text for the file}"
                ).atPosition(".", 1, 1)
                .results("");
        Assertions.assertEquals("hello, this is the text for the file", Files.readString(Paths.get("target/catoutput.txt")));
    }

    @Test
    @DisplayName("Executing 'echo hello' print out 'hello' as the macro result")
    void testOutput() throws Exception {
        System.setProperty("exec", "echo");
        TestThat.theInput("" +
                        "{@io:exec command=EXEC argument=\"hello\"}"
                ).atPosition(".", 1, 1)
                .results("hello");
    }

    @Test
    @DisplayName("Executing 'sleep 1; echo hello' print out 'hello' as the macro result only when synchronous")
    void testOutputAsync() throws Exception {
        System.setProperty("exec", "sh");
        Files.write(Paths.get("target/async.sh"), "sleep 1\necho hello".getBytes(StandardCharsets.UTF_8));
        TestThat.theInput("" +
                        "{@io:exec command=EXEC argument=target/async.sh}"
                ).atPosition(".", 1, 1)
                .results("hello");
        final var start = System.currentTimeMillis();
        TestThat.theInput("" +
                        "{@io:exec asynch=PROC001 command=EXEC argument=target/async.sh}"
                ).atPosition(".", 1, 1)
                .results("");
        final var runTime = System.currentTimeMillis() - start;
        Assertions.assertTrue(runTime < 1000);
    }

}
