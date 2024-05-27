package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestRange {

    private static String ref;

    @BeforeAll
    static void getRoot() throws IOException {
        ref = DocumentConverter.getRoot() + "/jamal-xls/src/test/test.jam";
    }

    @Test
    void testRangeHorizontalOneRow() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C1 horizontal}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!B1,Sheet1!C1");
    }

    @Test
    void testRangeHorizontalDefaultOneRow() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C1}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!B1,Sheet1!C1");
    }

    @Test
    void testRangeVerticalOneRow() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C1 vertical}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!B1,Sheet1!C1");
    }

    @Test
    void testRangeHorizontalOneCol() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:A3 horizontal}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!A2,Sheet1!A3");
    }

    @Test
    void testRangeHorizontalDefaultOneCol() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:A3}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!A2,Sheet1!A3");
    }

    @Test
    void testRangeVerticalOneCol() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:A3 vertical}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!A2,Sheet1!A3");
    }


    @Test
    void testRangeVertical() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C3 vertical}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!A2,Sheet1!A3,Sheet1!B1,Sheet1!B2,Sheet1!B3,Sheet1!C1,Sheet1!C2,Sheet1!C3");
    }

    @Test
    void testRangeHorizontal() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C3 horizontal}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!B1,Sheet1!C1,Sheet1!A2,Sheet1!B2,Sheet1!C2,Sheet1!A3,Sheet1!B3,Sheet1!C3");
    }

    @Test
    void testRangeHorizontalDefault() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C3}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1,Sheet1!B1,Sheet1!C1,Sheet1!A2,Sheet1!B2,Sheet1!C2,Sheet1!A3,Sheet1!B3,Sheet1!C3");
    }

    @Test
    void testRangeHorizontalDefaultSep() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C3 separator=|}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1|Sheet1!B1|Sheet1!C1|Sheet1!A2|Sheet1!B2|Sheet1!C2|Sheet1!A3|Sheet1!B3|Sheet1!C3");
    }

    @Test
    void testRangeHorizontalDefaultSheetDefined() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range sheet=Sheet2 region=Sheet1!A1:C3}")
                .atPosition(ref, 1, 1)
                .throwsBadSyntax("There is a sheet defined and it different from the sheet in the cell reference. 'Sheet2' != 'Sheet1'");
    }

    @Test
    void testRangeHorizontalDefaultSheetDefinedMulti() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C3 multi}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1|Sheet1!B1|Sheet1!C1," +
                        "Sheet1!A2|Sheet1!B2|Sheet1!C2," +
                        "Sheet1!A3|Sheet1!B3|Sheet1!C3");
    }
    @Test
    void testRangeVerticalSheetDefinedMulti() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:range region=Sheet1!A1:C3 multi vertical}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1|Sheet1!A2|Sheet1!A3," +
                        "Sheet1!B1|Sheet1!B2|Sheet1!B3," +
                        "Sheet1!C1|Sheet1!C2|Sheet1!C3");
    }

    @Test
    void testRangeForLoop() throws Exception {
        TestThat.theInput("{@xls:open file=../../README.xlsx}\n" +
                        "{!@for [evalist] S in ({@xls:range region=A1:C6 vertical})=S={@xls:cell S}\n" +
                        "}")
                .atPosition(ref, 1, 1)
                .results("\n" +
                        "README!A1=This is the content of the cell A1.\n" +
                        "README!A2=\n" +
                        "README!A3=\n" +
                        "README!A4=\n" +
                        "README!A5=\n" +
                        "README!A6=\n" +
                        "README!B1=\n" +
                        "README!B2=\n" +
                        "README!B3=\n" +
                        "README!B4=second will be deleted\n" +
                        "README!B5=\n" +
                        "README!B6=\n" +
                        "README!C1=\n" +
                        "README!C2=\n" +
                        "README!C3=\n" +
                        "README!C4=\n" +
                        "README!C5=\n" +
                        "README!C6=third will get to the second column\n");
    }
}
