package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * These tests can be executed during build, but there is no way to check that they work.
 * It is because they require manual assertions that the created XLS really looks the way it is supposed to look.
 * These tests are for the developer to run manually.
 */
public class TestManual {

    @Test
    @DisplayName("Manual test creating a new XLSX file and setting different cells")
    void manualTest() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var path = Path.of(root + "/jamal-xls/src/test/resources/manual_test.xlsx");
        if (Files.exists(path)) {
            Files.delete(path);
        }
        final var input = Files.readString(Path.of(root + "/jamal-xls/src/test/resources/manual_test.jam"));
        final var res = TestThat.theInput(input)
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results();
        Files.writeString(Path.of(root + "/jamal-xls/src/test/resources/manual_test.txt"), res, StandardCharsets.UTF_8);
        Assertions.assertTrue(Files.exists(path));
    }

}
