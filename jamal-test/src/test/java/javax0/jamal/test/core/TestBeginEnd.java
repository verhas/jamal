package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestBeginEnd {
    @Test
    void testGoodOne() throws Exception {
        TestThat.theInput("{@define a=1}{a}{@begin azaza}{a}{@define a=2}{a}{@end azaza}{a}").results("1121");
    }

    @Test
    void testUnclosedOneInInclude() throws Exception {
        TestThat.theInput("{@include res:unclosedbegin.jim}").throwsBadSyntax();
    }

    @Test
    void testUnclosedOneInImport() throws Exception {
        TestThat.theInput("{@import res:import/unclosedbegin.jim}").throwsBadSyntax();
    }

    @Test
    void testUnclosedOne() throws Exception {
        TestThat.theInput("{@define a=1}{a}{@begin azaza}{a}{@define a=2}{a}").throwsBadSyntax();
    }
}
