package javax0.jamal.xls;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestSamples {

    @TestFactory
    JamalTests<?> testSamples() {
        return JamalYamlTest.factory(
                "TestXls");
    }

}
