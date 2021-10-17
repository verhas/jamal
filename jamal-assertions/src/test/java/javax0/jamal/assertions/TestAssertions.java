package javax0.jamal.assertions;


import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestAssertions {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
            "TestEquals",
            "TestEmpty",
            "TestContains",
            "TestFail",
            "TestNumeric",
            JamalYamlTest.__OFF__
        );
    }
}

