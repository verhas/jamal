package javax0.jamal.groovy;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import javax0.jamal.testsupport.SentinelSmith;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.*;

public class TestGroovyMacros {

    static SentinelSmith blade;

    @BeforeEach
    void setUp() throws Exception {
        blade = SentinelSmith.forge("groovy");
    }

    @AfterAll
    static void afterAll() throws Exception {
        blade.close();
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
