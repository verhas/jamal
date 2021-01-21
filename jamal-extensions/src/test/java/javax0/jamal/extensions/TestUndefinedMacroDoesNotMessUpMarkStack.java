package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUndefinedMacroDoesNotMessUpMarkStack {

    @Test
    @DisplayName("When there is an undefind macro during the evaluation of a macro the mark stack is not messed up")
    void test() throws Exception {
        TestThat.theInput("{@try! {@define a={@undefined}}{a}}").results("There is no built-in macro with the id 'undefined'");
    }
}
