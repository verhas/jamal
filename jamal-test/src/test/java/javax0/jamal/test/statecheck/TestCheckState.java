package javax0.jamal.test.statecheck;

import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestCheckState {

    @Test
    @DisplayName("Statelessness is checked on macro load and throws runtime exception")
    void testCheckState() throws Exception {
        Assertions.assertThrows(RuntimeException.class, () -> {
            TestThat.theInput("{@use javax0.jamal.test.examples.StatefulBadMacro as macro}")
                    .results("");
        });
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
