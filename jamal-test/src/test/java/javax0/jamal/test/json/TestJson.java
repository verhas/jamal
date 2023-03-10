package javax0.jamal.test.json;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestJson {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
            "TestSet",
            "TestDump",
            "TestImport",
            "TestDefine",
            "TestAdd",
            "TestGet",
            JamalYamlTest.__OFF__
        );
    }
}
