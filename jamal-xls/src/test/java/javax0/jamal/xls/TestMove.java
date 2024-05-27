package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestMove {

    private static String ref;

    @BeforeAll
    static void getRoot() throws IOException {
        ref = DocumentConverter.getRoot() + "/jamal-xls/src/test/test.jam";
    }

    @Test
    void testMoveUp() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (north) A2}")
                .atPosition(ref, 1, 1)
                .results("A1");

    }

    @Test
    void testMoveUpWithSheet() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (north sheet=Sheet1) A2}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1");
    }
    @Test
    void testMoveUpWithSheet2() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (north) SHiraka!A1}")
                .atPosition(ref, 1, 1)
                .throwsBadSyntax("The cell is out of the sheet");
    }

    @Test
    void testMoveDown() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (south) A2}")
                .atPosition(ref, 1, 1)
                .results("A3");

    }

    @Test
    void testMoveDownWithSheet() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (down sheet=Sheet1) A2}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A3");
    }
    @Test
    void testMoveDownWithSheet2() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (south) SHiraka!A1048577}")
                .atPosition(ref, 1, 1)
                .throwsBadSyntax("The cell is out of the sheet");
    }


    @Test
    void testMoveLeft() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (left) B1}")
                .atPosition(ref, 1, 1)
                .results("A1");

    }

    @Test
    void testMoveLeftWithSheet() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (left sheet=Sheet1) B1}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!A1");
    }

    @Test
    void testMoveLeftWithSheet2() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (west) SHiraka!A1}")
                .atPosition(ref, 1, 1)
                .throwsBadSyntax("The cell is out of the sheet");
    }


    @Test
    void testMoveRigth() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (right) A1}")
                .atPosition(ref, 1, 1)
                .results("B1");

    }

    @Test
    void testMoveRightWithSheet() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (right sheet=Sheet1) B1}")
                .atPosition(ref, 1, 1)
                .results("Sheet1!C1");
    }

    @Test
    void testMoveRightWithSheet2() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (east) SHiraka!XFE1}")
                .atPosition(ref, 1, 1)
                .throwsBadSyntax("The cell is out of the sheet");
    }


    @Test
    void testMoveUp3() throws Exception {
        TestThat.theInput("{@xls:open file=resources/tobemerged.xlsx READ}" +
                        "{@xls:move (north N=3) A4}")
                .atPosition(ref, 1, 1)
                .results("A1");

    }
}
