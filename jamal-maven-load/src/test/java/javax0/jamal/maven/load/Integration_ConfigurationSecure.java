package javax0.jamal.maven.load;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * This integration test should be executed when the environment is configured in a way so that the configuration
 * is secure.
 * <p>
 * It uses the version 2.8.2, which was the latest release by the time of writing this file. The actual version does
 * not matter as long as it is available from the central repo.
 */
public class Integration_ConfigurationSecure {
    @DisplayName("Secure configuration runs any maven:load")
    @Test
    void test() throws Exception {
        TestThat.theInput("{@maven:load com.javax0.jamal:jamal-test:2.8.2}")
                .results("");
    }

}
