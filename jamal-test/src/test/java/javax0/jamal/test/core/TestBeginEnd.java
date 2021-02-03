package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestBeginEnd {
    @Test
    void testGoodOne() throws Exception {
        TestThat.theInput("{@define a=1}" +
            "{a}" + // this is 1 as defined
            "{@begin azaza}" +
            "{a}" + // a new scope started but the definition was not yet overwritten, it is still 1
            "{@define a=2}" +
            "{a}" + // value overwritten inside the scope, it is 2
            "{@end azaza}" +
            "{a}" // scope ended, orignal value was restored, it is 1
        ).results("1121");
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
