package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUnicode {

    @Test
    @DisplayName("Test a few unicode characters")
    void testUnicodeCharacters() throws Exception{
        TestThat.theInput("" +
                "{@unicode FF70}" +
                "{@unicode FE70}" +
                "").results("\uFF70\uFE70");
    }

    @Test
    @DisplayName("Test a space decimal")
    void testUnicodeSpaceDecimal() throws Exception{

        TestThat.theInput("" +
                "{@unicode &#32}" +
                "").results(" ");
    }

    @Test
    @DisplayName("Test an invalid unicode character")
    void testInvalidUnicodeCharacters() throws Exception{
        TestThat.theInput("" +
                "{@unicode &#x11FFFF}" +
                "").throwsBadSyntax();
    }
}
