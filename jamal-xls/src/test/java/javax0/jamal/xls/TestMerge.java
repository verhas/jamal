package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestMerge {

    @Test
    void testSheetRowColumnCell() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var outputPath = Path.of(root + "/jamal-xls/src/test/resources/mergedSomething.xlsx");
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
        }
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx out=resources/mergedSomething.xlsx}" +
                        "{@xls:merge region=C7:D9}"+
                        "{@xls:merge top=6 bottom=8 left=5 right=6}" +
                        "{@xls:merge sheet=Sheet2 region=C7:D9}"+
                        "{@xls:merge sheet=Sheet2 top=6 bottom=8 left=5 right=6}" +
                        "{@xls:merge region=Sheet3!C7:D9}" +
                        "{@xls:merge region=Sheet3!F7:Sheet3!G9}"
                )
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results();
        Assertions.assertTrue(Files.exists(outputPath));
    }


}
