package javax0.jamal.test.integration;

import javax0.jamal.api.EnvironmentVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Integration_GetEnv {
    @Test
    @DisplayName("EnvironmentVariables.getenv() reads the ~/.jamal/setting.properties file")
    void testProperties() {
        Assertions.assertEquals("Peter Verhas' macbook", EnvironmentVariables.getenv("jamal.testproperty").orElse(null));
    }
}
