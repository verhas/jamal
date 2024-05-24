package javax0.jamal.test.extra;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * These tests check that when a user defined macro is not defined and this error happens inside an `try` macro then the
 * push/pop stack of the scopes does not get messed up.
 */
public class TestUndefinedMacroDoesNotMessUpMarkStack {

    @Test
    @DisplayName("When there is an undefined macro during the evaluation of a macro the mark stack is not messed up")
    void test() throws Exception {
        TestThat.theInput("{@try! {@define a={@undefined}}{a}}").results("There is no built-in macro with the id 'undefined'; did you mean '@undefine'?");
    }

    @Test
    @DisplayName("When there is an undefined macro during the evaluation of a macro the mark stack is not messed up")
    void test2() throws Exception {
        TestThat.theInput("{@try! {@define a={undefined}}{a}}").results("User macro '{undefined ...' is not defined. Did you mean '@undefine'?");
    }
}
