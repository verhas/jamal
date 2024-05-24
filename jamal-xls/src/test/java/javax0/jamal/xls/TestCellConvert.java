package javax0.jamal.xls;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestCellConvert {

    @Test
    public void testToRow() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        TestThat.theInput("{@xls:row A1}").results("0");
    }

    @Test
    public void testToCol() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        TestThat.theInput("{@xls:col A1}").results("0");
    }

    @Test
    public void testToSheet() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        TestThat.theInput("{@xls:sheet Sheet1!A1}").results("Sheet1");
    }

    @Test
    public void testToCell() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        TestThat.theInput("{@xls:to:cell row=0 col=0}\n" +
                "{@xls:to:cell row=0 col=0 rowAbsolute}\n"+
                "{@xls:to:cell row=0 col=0 colAbsolute}\n"+
                "{@xls:to:cell row=0 col=0 colAbsolute rowAbsolute}\n"+
                "{@xls:to:cell row=0 col=0 sheet=\"Abrakadabra\"}\n"
        ).results("A1\nA$1\n$A1\n$A$1\nAbrakadabra!A1\n");
    }

    @Test
    public void testToSheetMissing() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        TestThat.theInput("{@xls:sheet A1}").throwsBadSyntax("Cannot convert the cell reference");
    }


}
