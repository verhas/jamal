package javax0.jamal.maven.load;

import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * This integration test should be executed when the environment is configured in a way so that the configuration
 * is not secure.
 */
public class Integration_ConfigurationSecure {
    @DisplayName("Non-secure configuration fails any maven:load")
    @Test
    void test()throws Exception {
        TestThat.theInput(String.format( "{@maven:load com.javax0.jamal:jamal-test:%s}", Processor.jamalVersionString()))
                .results("");
    }

}
