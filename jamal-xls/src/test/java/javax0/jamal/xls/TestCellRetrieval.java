package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestCellRetrieval {

    public static final String TEST_XLSX_CONTENT;

    static {
        try {
            TEST_XLSX_CONTENT = Files.readString(Paths.get(DocumentConverter.getRoot() + "/jamal-xls/src/test/resources", "test.xlsx.text")).replaceAll("\r\n", "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String[] MODIFIERS = {"",
            "value", "content", "type", "format", "comment", "commentAuthor", "hasComment", "isString", "isNumeric", "isBoolean", "isFormula", "isBlank", "isError", "isNull",
            "style",
            "style toString",
            "style align",
            "style border",
            "style fill",
            "style dataFormat",
            "style hidden",
            "style locked",
            "style rotation",
            "style shrinkToFit",
            "style verticalAlignment",
            "style wrapText",
    };

    @Test
    void testCellRetrievalByRefName() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var sb = new StringBuilder();
        for (int i = 1; i <= 6; i++) {
            for (final var mod : MODIFIERS) {
                sb.append("a").append(i).append("(").append(mod).append(")={@xls:cell (").append(mod).append(") A").append(i).append("}\n");
            }
        }
        TestThat.theInput("{@xls:open file=resources/test.xlsx}" + sb)
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results(TEST_XLSX_CONTENT);
    }

    @Test
    void testCellRetrievalByIndices() throws Exception {
        final var root = DocumentConverter.getRoot();
        final var sb = new StringBuilder();
        for (int i = 0; i <= 5; i++) {
            for (final var mod : MODIFIERS) {
                sb.append("a").append(i + 1).append("(").append(mod).append(")={@xls:cell (").append(mod).append(" col=0 row=").append(i).append(")}\n");
            }
        }
        TestThat.theInput("{@xls:open file=resources/test.xlsx}" + sb)
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results(TEST_XLSX_CONTENT);
    }

    @Test
    void testCellRetrievalBySheetName() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{@xls:open file=resources/test.xlsx}" +
                        "{@xls:cell (sheet=Sheet1)C2}")
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results("Bambula");
    }

    @Test
    void testCellRetrievalBBoolean() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{@xls:open file=resources/test.xlsx}" +
                        "{@xls:cell (content) E3}" +
                        "{@xls:cell (content) E4}")
                .atPosition(root + "/jamal-xls/src/test/test.jam", 1, 1)
                .results("true");
    }

}

