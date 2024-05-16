package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestDelete {

    @Test
    void testSheetRowColumnCell() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var outputPath = Path.of(root + "/jamal-xls/src/test/resources/deletedSomething.xlsx");
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
        }
        TestThat.theInput("{@xls:open file=resources/tobedeleted.xlsx out=resources/deletedSomething.xlsx}" +
                        "{@xls:delete sheet=Sheet2}" +
                        "{@xls:delete row=2}" +
                        "{@xls:delete col=7}" +
                        "{@xls:delete cell=Sheet3!C7}" +
                        "{@xls:delete ROW cell=Sheet4!A3}" +
                        "{@xls:delete COL cell=Sheet4!H1}"+
                        "{@xls:delete ROW sheet=Sheet5 cell=A3}" +
                        "{@xls:delete COL sheet=Sheet5 cell=H1}"
                )
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results();
        Assertions.assertTrue(Files.exists(outputPath));
    }


}
