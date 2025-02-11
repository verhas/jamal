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
