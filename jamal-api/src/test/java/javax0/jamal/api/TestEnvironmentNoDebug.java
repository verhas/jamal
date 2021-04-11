package javax0.jamal.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class TestEnvironmentNoDebug {
    @Test
    @DisplayName("The environment variable JAMAL_DEBUG is not set")
    void test() {
        Assertions.assertNull(
            Optional.ofNullable(System.getenv(Debugger.JAMAL_DEBUG_ENV)).orElseGet(
                () -> System.getProperty(Debugger.JAMAL_DEBUG_SYS)
            ));
    }
}
