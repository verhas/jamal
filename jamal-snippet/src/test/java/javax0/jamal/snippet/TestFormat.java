package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestFormat {
    @Test
    @DisplayName("Format text and parameters")
    void testFormat() throws Exception{
        TestThat.theInput("{@format /%d is a number and %s is a string/(int)13/\"wuff\"}")
            .results("13 is a number and \"wuff\" is a string");
    }

    @Test
    @DisplayName("Format throws up for bad code")
    void testFormatThrowUp() throws Exception{
        TestThat.theInput("{@format /%d is a number and %s is a string/13/\"wuff\"}")
            .throwsBadSyntax();
    }
}
