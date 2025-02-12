package javax0.jamal.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * This class contains a JUnit test to verify that the integration test run is recent and not stale.
 * <p>
 * The test checks for the presence and validity of a timestamp file that indicates the last successful
 * execution of the integration tests. If the timestamp is older than 24 hours, the test fails.
 * </p>
 * <p>
 * The test is configured to run only on a specific development environment (identified by the
 * presence of "verhasp" in the absolute path). It is also skipped if executed in a SNAPSHOT version.
 * </p>
 *
 * <h2>Test Behavior</h2>
 * <ul>
 *     <li>Verifies that the integration test timestamp file exists.</li>
 *     <li>Ensures the file content is not null and starts with "OK".</li>
 *     <li>Parses the timestamp from the file and checks that it is within the last 24 hours.</li>
 * </ul>
 */
public class TestIntegrationTestRun {

    private static final Path FILE_PATH = Path.of("../jamal-test/IT_DOCKER/integration_test.run");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    @DisplayName("Test that the integration test is not stale")
    void testIntegrationTestNotStale() throws IOException {
        // It is an error to have a stale integration test run only on the release
        Assumptions.assumeTrue(!Processor.jamalVersionString().endsWith("-SNAPSHOT"));
        // we do not want to run it on GitHub, only on my dev machine
        Assumptions.assumeTrue(new File(".").getAbsolutePath().contains("verhasp"));

        Assertions.assertTrue(Files.exists(FILE_PATH), "There is no integration test file time stamp");

        String content = Files.readString(FILE_PATH).trim();
        Assertions.assertNotNull(content, "Integration test time stamp file content is null");

        String[] parts = content.split(" ", 2);
        Assertions.assertEquals("OK", parts[0], "Integration test time stamp file content is not OK");

        LocalDateTime fileTime = LocalDateTime.parse(parts[1], FORMATTER);
        LocalDateTime now = LocalDateTime.now();

        long hoursDiff = ChronoUnit.HOURS.between(fileTime, now);
        Assertions.assertTrue(hoursDiff < 24, "Integration test time stamp too old.");
    }
}
