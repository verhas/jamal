package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestFind {

    @Test
    void testFind() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{@xls:open READ file=findSomething.xlsx}" +
                        "01 {@xls:find (cell=G30 reverse inCol $=zz limitRow=20 orElse=\"arbad akarba\")t{zz}}\n" +
                        "02 {@xls:find (cell=G1 inCol $=zz limitRow=20 orElse=\"abraka dabra\")t{zz}}\n" +
                        "03 {@xls:find (cell=A1 empty inRow)}\n" +
                        "04 {@xls:find (cell=A3 empty inRow)}\n" +
                        "05 {@xls:find (cell=G4 blank inCol)}\n" +
                        "06 {@xls:find (cell=G4 empty inCol)}\n" +
                        "07 {@xls:find (cell=G4 blank inCol reverse)}\n" +
                        "08 {@xls:find (cell=G1 inCol string)3}\n" +
                        "09 {@xls:find (cell=G1 inCol number)3.3}\n" +
                        "10 {@xls:find (cell=G1 inCol integer)3}\n" +
                        "11 {@xls:find (cell=G1 inCol number)3}\n" +
                        "12 {@xls:find (cell=G1 inCol regex)\\d+}\n" +
                        "13 {@xls:find (cell=G1 inCol $=zz)t{zz}}\n" +
                        "")
                .atPosition(root + "/jamal-xls/src/test/resources/README.adoc.jam", 1, 1)
                .results(
                        "01 arbad akarba\n" +
                                "02 abraka dabra\n" +
                                "03 Sheet1!A1\n" +
                                "04 Sheet1!B3\n" +
                                "05 Sheet1!G26\n" +
                                "06 Sheet1!G27\n" +
                                "07 Sheet1!G3\n" +
                                "08 Sheet1!G5\n" +
                                "09 Sheet1!G4\n" +
                                "10 Sheet1!G5\n" +
                                "11 Sheet1!G5\n" +
                                "12 Sheet1!G5\n" +
                                "13 Sheet1!G31\n"+
                        ""
                );
    }
    
    @Test
    void testFind2() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{@xls:open file=log_readme.xlsx WRITE}" +
                        "{#xls:set (cell={@xls:find (inCol cell=A1 empty)}){@date yyyy-MM-dd HH:mm:ss}}"
                        )
                .atPosition(root + "/jamal-xls/src/test/resources/README.adoc.jam", 1, 1)
                .results(""
                );
    }
    
    
    
}
