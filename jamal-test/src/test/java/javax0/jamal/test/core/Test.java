package javax0.jamal.test.core;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class Test {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
            "TestFor",
            "TestBeginEnd",
            "TestBlock",
            "TestComment",
            "TestDefine",
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
            "TestUserDefined",
            "TestSep",
            "nested_ud_macros"
        );
    }
}
