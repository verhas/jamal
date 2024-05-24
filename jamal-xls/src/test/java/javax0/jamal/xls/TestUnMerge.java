package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestUnMerge {

    @Test
    void testSheetRowColumnCell() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var outputPath = Path.of(root + "/jamal-xls/src/test/resources/unmergedSomething.xlsx");
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
        }
        TestThat.theInput("{@xls:open file=resources/tobeunmerged.xlsx out=resources/unmergedSomething.xlsx}" +
                        "{@xls:unmerge D7}"+
                        "{@xls:unmerge (row=8 col=6)}"
                )
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results();
        Assertions.assertTrue(Files.exists(outputPath));
    }


}
