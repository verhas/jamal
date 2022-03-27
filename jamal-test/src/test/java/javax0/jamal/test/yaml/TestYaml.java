package javax0.jamal.test.yaml;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestYaml {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
            "TestSet",
            "TestDump",
            "TestImport",
            "TestResolve",
            "TestDefine",
            "TestAdd",
            "TestGet",
            "TestXml",
            JamalYamlTest.__OFF__
        );
    }
}
