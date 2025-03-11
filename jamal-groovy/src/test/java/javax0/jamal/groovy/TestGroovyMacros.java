package javax0.jamal.groovy;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import javax0.jamal.testsupport.SentinelSmith;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class TestGroovyMacros {

    @BeforeEach
    void setUp() throws Exception {
        SentinelSmith.forge("groovy");
    }

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
                "TestGroovyMacros"
        );
    }

    // snippet sample_snippet
    @Test
    @DisplayName("Test a simple groovy eval")
    void testSimpleEval() throws Exception {
        TestThat.theInput("{@groovy:eval 6+3}").results("9");
    }
    // end snippet
}
