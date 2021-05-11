package javax0.jamal.test.yaml;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class TestXml {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
            "TestXml"
        );
    }
}
