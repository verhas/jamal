package javax0.jamal.mavenextension;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestCustomModelProcessor {

    private static boolean isIntelliJ() {
        final var st = new Exception().getStackTrace();
        for (final var s : st) {
            if (s.getClassName().contains("Idea")) {
                return true;
            }
        }
        return false;
    }

    private static void touch(String time) throws IOException {
        Files.write(Paths.get(DocumentConverter.getRoot() + "/.mvn", "touch"), time.getBytes());
    }

    /**
     * Create a touch file in the .mvn directory to signal that the extension was executed if the test is run from IntelliJ.
     *
     * @throws IOException if the file cannot be written
     */
    @BeforeEach
    void manuallyCreateTouchFile() throws IOException {
        if (isIntelliJ()) {
            touch(String.valueOf(System.currentTimeMillis()));
        }
    }


    /**
     * This test will do nothing during the maven build.
     * <p>
     * The next test checks the integrity of the already DEPLOYED build.
     * If the test fails, a fix has to be created and then a new build has to start.
     * And that build will still use the same DEPLOYED build and will fail.
     * <p>
     * To overcome this situation, this test can be executed from IntelliJ.
     * It will create a touch file with the current timestamp.
     * The next test will not see that the DEPLOYED build is broken and lets the current one to build and deploy.
     * <p>
     *
     * @throws IOException if the file cannot be written
     */
    @Test
    @DisplayName("Create a current touch file but only in IntelliJ")
    void createTouchFile() throws IOException {
    }

    @Test
    @DisplayName("Test that the extension was running in the Jamal project")
    public void testItWasExecuted() throws IOException {
        final var root = DocumentConverter.getRoot();
        final var lines = Files.readAllLines(Paths.get(root + "/.mvn", "touch"));
        final var ts = Long.parseLong(lines.get(0)); // throw exception if not a number, fail the test
        final var now = System.currentTimeMillis();
        Assertions.assertTrue(ts <= now, "The touch file was not created or the timestamp is in the future");
        if (isIntelliJ()) {
            Assertions.assertEquals(0, ts, "We run in IntelliJ and the touch file time stamp is not zero");
        } else {
            Assertions.assertTrue(now - ts < 1000 * 60 * 10, "The touch file is older than 10 minutes, fix the bug, and manually run the 'createTouchFile' test from IntelliJ.");
        }
        // not to have a lingering current value in the touch file
        // but write three zeroes to signal it was by the test, and not a manual reset calling
        touch("000");
    }
}
