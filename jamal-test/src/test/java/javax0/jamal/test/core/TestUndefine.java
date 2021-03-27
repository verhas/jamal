package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUndefine {

    @Test
    @DisplayName("Test that a local macro can be undefined")
    void testLocalUndefine() throws Exception {
        TestThat.theInput("{@define fruit=apple}{fruit}{@undefine fruit}{?fruit}").results("apple");
        TestThat.theInput("{@define fruit=apple}{fruit}{@undefine fruit}{fruit}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that a global macro can be undefined")
    void testGlobalUndefine() throws Exception {
        TestThat.theInput("{@define :fruit=apple}{fruit}{@undefine fruit}{?fruit}").results("apple");
        TestThat.theInput("{@define :fruit=apple}{fruit}{@undefine :fruit}{?:fruit}").results("apple");
        TestThat.theInput("{@define :fruit=apple}{fruit}{@undefine fruit}{fruit}").throwsBadSyntax();
        TestThat.theInput("{@define :fruit=apple}{fruit}{@undefine :fruit}{:fruit}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that a global macro can be undefined locally")
    void testGlobalLocallyUndefine() throws Exception {
        TestThat.theInput("{@define :fruit=apple}{fruit} " +
            "{#ident {@undefine fruit} *{?fruit}* }" +
            "{fruit}").results("apple ** apple");
    }

    @Test
    @DisplayName("Undefinedness can be exported")
    void testExportUndefine() throws Exception {
        TestThat.theInput("{@define :fruit=apple}{fruit} " +
            "{#ident {@undefine fruit} *{?fruit}* {@export fruit}}" +
            "{?fruit}").results("apple ** ");
    }

    @Test
    @DisplayName("Test that a global macro can be undefined globally from local place")
    void testGlobalGloballyLocalUndefine() throws Exception {
        TestThat.theInput("{@define :fruit=apple}{fruit} {#ident {@undefine :fruit} *{?fruit}* }{?fruit}")
            .results("apple ** ");
    }

    @Test
    @DisplayName("Test that a global macro can be undefined globally from local place")
    void testLocalGloballyLocalUndefine() throws Exception {
        TestThat.theInput("{#ident {@define fruit=apple}{@undefine :fruit} *{?fruit}*}{?fruit}")
            .results("*apple*");
    }
}
