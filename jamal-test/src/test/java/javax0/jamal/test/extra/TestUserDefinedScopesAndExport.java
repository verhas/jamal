package javax0.jamal.test.extra;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that the macros defined in the parameters of a user defined macro are available in for the evaluation of the
 * user defined macro itself and that export also works as expected.
 */
public class TestUserDefinedScopesAndExport {

    @Test
    @DisplayName("Macro defined in a user defined macro parameter can be used in the macro content")
    void testUserDefinedScopeLocking() throws Exception {
        TestThat.theInput(
            "{@define a={b}}{a {@define b=this is b}}"
        ).results("this is b");
    }

    @Test
    @DisplayName("Macro defined in a user defined macro parameter can be used in the macro content for macro with one argument")
    void testUserDefinedScopeLockingOneArgument() throws Exception {
        TestThat.theInput(
            "{@define a($x)={$x}}{a {@define b=this is b}b}"
        ).results("this is b");
    }

    @Test
    @DisplayName("Macro defined in a user defined macro parameter can be used in the macro content for macro with two arguments")
    void testUserDefinedScopeLockingTwoArgumentas() throws Exception {
        TestThat.theInput(
            "{@define a($x,$y)={$x$y}}{a /{@define bd=this is b}b/d}"
        ).results("this is b");
    }

    @Test
    @DisplayName("Macro defined in a user defined macro parameter is not visible outside")
    void testUserDefinedScopeLockingNonExport() throws Exception {
        TestThat.theInput(
            "{@define a={b}}{a {@define b=this is b}}{b}"
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("Macro defined in a user defined macro parameter is visible outside when exported")
    void testUserDefinedScopeLockingExport() throws Exception {
        TestThat.theInput(
            "{@define a={b}}{a {@define b=this is b}{@export b}} {b}"
        ).results("this is b this is b");
    }

}
