package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestIf {

    @Test
    void testIf()throws Exception{
        TestThat.theInput(
            "{@define enclose(a)=<||a||>}{@define z=13}\n" +
                "{enclose {#if || |{z}}}\n" +
                "{enclose {#if |||{z}}}"
        ).results(
            "\n" +
                "<||13||>\n" +
                "<||13||>"
        );
    }

    @Test
    @DisplayName("Test if with regular expression separator")
    void testRegExpSeparator() throws Exception {
        TestThat.theInput("{#if`///`1///aa}").results("aa");
    }
}
