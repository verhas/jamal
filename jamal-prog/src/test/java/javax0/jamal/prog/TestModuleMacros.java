package javax0.jamal.prog;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestModuleMacros {


    @TestFactory
    JamalTests<?> testSamples() {
        return JamalYamlTest.factory(
                "TestSamples");
    }
}
