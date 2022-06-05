package javax0.jamal.mock;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestFactory;

public class TestMock {

    @TestFactory
    @DisplayName("Test the different mock functionalities")
    JamalTests<?> testMocks() {
        return JamalYamlTest.factory(
                "TestMocks");
    }

}
