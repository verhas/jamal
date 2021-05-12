package javax0.jamal.test.core;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestCore {

    @TestFactory
    JamalTests<?> testAllCoreMacros() {
        return JamalYamlTest.factory(
            "TestDefine",
            "TestFor",
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
            "nested_ud_macros"
        );
    }
}
