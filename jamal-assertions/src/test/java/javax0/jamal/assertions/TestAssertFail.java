package javax0.jamal.assertions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestAssertFail {

    @Test
    @DisplayName("simple fail")
    void testSimpleFailWithMessage() throws Exception {
        TestThat.theInput("{@assert:fail huhh}").throwsBadSyntax(".*huhh.*");
    }

    @Test
    @DisplayName("simple fail without message")
    void testSimpleFailWithoutMessage() throws Exception {
        TestThat.theInput("{@assert:fail}").throwsBadSyntax(".*assert:fail has failed.*");
    }

    @Test
    @DisplayName("negated fail, which makes nonsense, but here it is")
    void testSimpleFailNot() throws Exception {
        TestThat.theInput("{@assert:fail (not) it makes no sense but the option 'not' is available for all assertions}")
            .results("");
    }
}
