package javax0.jamal.snippet;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestStringMacros {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
            "TestStringMacros"
        );
    }
}
