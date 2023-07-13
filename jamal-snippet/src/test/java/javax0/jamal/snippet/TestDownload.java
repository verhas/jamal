package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestDownload {

    @DisplayName("get the content of one file and download it to another")
    @Test
    void testDownload() throws Exception {
        final var src = "./target/src.bin";
        byte[] expected = {55, 13, 67};
        Files.write(Paths.get(src), expected, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        TestThat.theInput(String.format("{@download (file=target/downloaded.bin) %s}", src)).results("");
        final var downloaded = Files.readAllBytes(Paths.get(src));
        Assertions.assertArrayEquals(expected, downloaded);
    }
}
