package javax0.jamal.test.json;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestJson {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
            "TestImport",
            "TestDefine",
            "TestSet",
            "TestGet",
            "TestLength",
            "TestKeys",
            JamalYamlTest.__OFF__
        );
    }
}
