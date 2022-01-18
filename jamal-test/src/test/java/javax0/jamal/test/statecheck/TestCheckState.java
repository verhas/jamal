package javax0.jamal.test.statecheck;

import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class TestCheckState {

    @Test
    @DisplayName("Statelessness is checked on macro load and throws runtime exception")
    void testCheckState() throws Exception {
        Assertions.assertThrows(RuntimeException.class, () -> {
            TestThat.theInput("{@use javax0.jamal.test.examples.StatefulBadMacro as macro}")
                    .results("");
        });
    }

    /**
     * State checking stores the result for each class in a WeakHashMap. The second time the check does not repeat the
     * deep search of non-static and non-final fields. It uses the result from the first time. When it is done the field
     * violating the statelessness is not found and is not included in the error message.
     *
     * @throws Exception if the test fails
     */
    @Test
    @DisplayName("Statelessness is checked differently second time, even in different processors")
    void testCheckStateTwice() throws Exception {
        try {
            TestThat.theInput("{@use javax0.jamal.test.examples.StatefulBadMacro as macro}")
                    .results("");
            throw new AssertionFailedError("The exception was not thrown.");
        } catch (RuntimeException e) {
            // may or may not contain the field name in case other tests were already running
            Assertions.assertTrue(e.getMessage().startsWith("The macro class 'javax0.jamal.test.examples.StatefulBadMacro' is not stateless, it  has non-final, non-static field"));
        }
        try {
            TestThat.theInput("{@use javax0.jamal.test.examples.StatefulBadMacro as macro}")
                    .results("");
            throw new AssertionFailedError("The exception was not thrown.");
        } catch (RuntimeException e) {
            Assertions.assertEquals("The macro class 'javax0.jamal.test.examples.StatefulBadMacro' is not stateless, it  has non-final, non-static field.", e.getMessage());
        }
    }

    @Test
    @DisplayName("Statefulness check cannot be captured with the macro 'try'.")
    void testCheckStateInTry() throws Exception {
        Assertions.assertThrows(RuntimeException.class, () -> {
            TestThat.theInput("{@try {@use javax0.jamal.test.examples.StatefulBadMacro as macro}}")
                    .results("");
        });
    }

    @Test
    @DisplayName("Macro statefulness check can be switched off.")
    void testCheckStateSwitchOff() throws Exception {
        EnvironmentVariables.setenv(EnvironmentVariables.JAMAL_CHECKSTATE_ENV, "false");
        TestThat.theInput("{@use javax0.jamal.test.examples.StatefulBadMacro as macro}{@macro}.{@macro}.{@macro}")
                .results("1.2.3");
        EnvironmentVariables.resetenv(EnvironmentVariables.JAMAL_CHECKSTATE_ENV);
    }


    @Test
    @DisplayName("Statelessness is checked on global macro load and throws runtime exception")
    void testCheckStateForGLobal() throws Exception {
        Assertions.assertThrows(RuntimeException.class, () -> {
            TestThat.theInput("{@use global javax0.jamal.test.examples.StatefulBadMacro as macro}")
                    .results("");
        });
    }

    @Test
    @DisplayName("Macro statefulness check can be switched off also on global macro.")
    void testCheckStateSwitchOffForGlobal() throws Exception {
        EnvironmentVariables.setenv(EnvironmentVariables.JAMAL_CHECKSTATE_ENV, "false");
        TestThat.theInput("{@use global javax0.jamal.test.examples.StatefulBadMacro as macro}{@macro}.{@macro}.{@macro}")
                .results("1.2.3");
        EnvironmentVariables.resetenv(EnvironmentVariables.JAMAL_CHECKSTATE_ENV);
    }

}
