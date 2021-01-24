package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUndefinedMacroDoesNotMessUpMarkStack {

    @Test
    @DisplayName("When there is an undefined macro during the evaluation of a macro the mark stack is not messed up")
    void test() throws Exception {
        TestThat.theInput("{@try! {@define a={@undefined}}{a}}").results("There is no built-in macro with the id 'undefined'");
    }

    @Test
    @DisplayName("When there is an undefined macro during the evaluation of a macro the mark stack is not messed up")
    void test2() throws Exception {
        TestThat.theInput("{@try! {@define a={undefined}}{a}}").results("User defined macro '{undefined ...' is not defined.");
    }
}
