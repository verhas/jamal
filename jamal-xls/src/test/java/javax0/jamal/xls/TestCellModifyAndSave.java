package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;

public class TestCellModifyAndSave {


    @Test
    @DisplayName("Test open input and output")
    void testInputOutput() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var outputPath = Path.of(root + "/jamal-xls/src/test/resources/output.xlsx");
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
        }
        TestThat.theInput("{@xls:open file=resources/input.xlsx out=resources/output.xlsx}" +
                        "{@xls:set (cell=A1) madagaraskara}")
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results("");
        Assertions.assertTrue(Files.exists(outputPath));
    }

    @Test
    @DisplayName("Test open for write")
    void testUpdate() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var path = Path.of(root + "/jamal-xls/src/test/resources/updated.xlsx");
        if (Files.exists(path)) {
            Files.delete(path);
        }
        Files.copy(Path.of(root + "/jamal-xls/src/test/resources/input.xlsx"), path);
        final var original = MessageDigest.getInstance("sha-512").digest(Files.readAllBytes(path));
        TestThat.theInput("{@xls:open file=resources/updated.xlsx WRITE}" +
                        "{@xls:set (cell=A1) madagaraskara}")
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results("");
        Assertions.assertTrue(Files.exists(path));
        final var updated = MessageDigest.getInstance("sha-512").digest(Files.readAllBytes(path));
        Assertions.assertFalse(Arrays.equals(original, updated));
    }

    @Test
    @DisplayName("Test open for write non existent to create new file")
    void testCreateNew() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var path = Path.of(root + "/jamal-xls/src/test/resources/created.xlsx");
        if (Files.exists(path)) {
            Files.delete(path);
        }
        TestThat.theInput("{@xls:open file=resources/created.xlsx WRITE}" +
                        "{@xls:set (cell=A1) created}")
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results("");
        Assertions.assertTrue(Files.exists(path));
    }


}
