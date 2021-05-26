package javax0.jamal.test.core;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestCore {

    @TestFactory
    JamalTests<?> testAllCoreMacros() {
        return JamalYamlTest.factory(
            "TestFor",
            JamalYamlTest.__OFF__,
            "TestSamples",
            "TestDefine",
            "TestBeginEnd",
            "TestBlock",
            "TestComment",
            "TestEscape",
            "TestEval",
            "TestExport",
            "TestIdent",
            "TestIf",
            "TestImport",
            "TestJShell",
            "TestNewLineOptions",
            "TestOptions",
            "TestRecursiveMacro",
            "TestRequire",
            "TestTry",
            "TestUndefine",
            "TestUse",
            "TestSep",
            "TestEngine",
            "nested_ud_macros"
        );
    }
}
