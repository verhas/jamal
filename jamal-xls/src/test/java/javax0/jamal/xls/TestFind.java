package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestFind {

    @Test
    void testFind() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{@xls:open READ file=findSomething.xlsx}" +
                        "{@xls:find (cell=G30 reverse inCol $=zz limitRow=20 orElse=\"arbad akarba\")t{zz}}" +
                        "{@xls:find (cell=G1 inCol $=zz limitRow=20 orElse=\"abraka dabra\")t{zz}}" +
                        "{@xls:find (cell=A1 empty inRow)}" +
                        "{@xls:find (cell=A3 empty inRow)}" +
                        "{@xls:find (cell=G4 blank inCol)}" +
                        "{@xls:find (cell=G4 empty inCol)}" +
                        "{@xls:find (cell=G4 blank inCol reverse)}" +
                        "{@xls:find (cell=G1 inCol string)3}" +
                        "{@xls:find (cell=G1 inCol number)3.3}" +
                        "{@xls:find (cell=G1 inCol integer)3}" +
                        "{@xls:find (cell=G1 inCol number)3}" +
                        "{@xls:find (cell=G1 inCol regex)\\d+}" +
                        "{@xls:find (cell=G1 inCol $=zz)t{zz}}" +
                        "")
                .atPosition(root + "/jamal-xls/src/test/resources/README.adoc.jam", 1, 1)
                .results(
                        "arbad akarba" +
                        "abraka dabra" +
                        "Sheet1!A1" +
                        "Sheet1!B3" +
                        "Sheet1!G26" +  // there are some spaces in this cell
                        "Sheet1!G27" +
                        "Sheet1!G3" +
                        "Sheet1!G25" +
                        "Sheet1!G4" +
                        "Sheet1!G5" +
                        "Sheet1!G5"+
                        "Sheet1!G25"+ // the prior columns may loo '3' in Excel, but they are '3.0'
                        "Sheet1!G31"+
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
