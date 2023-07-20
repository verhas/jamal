package javax0.jamal.test.core;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestCore {

    @TestFactory
    JamalTests<?> testFor() {
        return JamalYamlTest.factory(
                "TestFor");
    }

    @TestFactory
    JamalTests<?> testSamples() {
        return JamalYamlTest.factory(
                "TestSamples");
    }

    @TestFactory
    JamalTests<?> testDefine() {
        return JamalYamlTest.factory(
                "TestDefine");
    }

    @TestFactory
    JamalTests<?> testDefineXtended() {
        return JamalYamlTest.factory(
                "TestDefineXtended");
    }

    @TestFactory
    JamalTests<?> testBlock() {
        return JamalYamlTest.factory(
                "TestBlock");
    }

    @TestFactory
    JamalTests<?> testComment() {
        return JamalYamlTest.factory(
                "TestComment");
    }

    @TestFactory
    JamalTests<?> testEscapep() {
        return JamalYamlTest.factory(
                "TestEscape");
    }

    @TestFactory
    JamalTests<?> testEval() {
        return JamalYamlTest.factory(
                "TestEval");
    }

    @TestFactory
    JamalTests<?> testExport() {
        return JamalYamlTest.factory(
                "TestExport");
    }

    @TestFactory
    JamalTests<?> testIdent() {
        return JamalYamlTest.factory(
                "TestIdent");
    }

    @TestFactory
    JamalTests<?> testIf() {
        return JamalYamlTest.factory(
                "TestIf");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosB() {
        return JamalYamlTest.factory(
                "TestJShell");
    }

    @TestFactory
    JamalTests<?> testOptions() {
        return JamalYamlTest.factory(
                "TestOptions");
    }

    @TestFactory
    JamalTests<?> testRecursiveMacro() {
        return JamalYamlTest.factory(
                "TestRecursiveMacro");
    }

    @TestFactory
    JamalTests<?> testRequire() {
        return JamalYamlTest.factory(
                "TestRequire");
    }

    @TestFactory
    JamalTests<?> testTry() {
        return JamalYamlTest.factory(
                "TestTry");
    }

    @TestFactory
    JamalTests<?> testUndefine() {
        return JamalYamlTest.factory(
                "TestUndefine");
    }

    @TestFactory
    JamalTests<?> testUse() {
        return JamalYamlTest.factory(
                "TestUse");
    }

    @TestFactory
    JamalTests<?> testSep() {
        return JamalYamlTest.factory(
                "TestSep");
    }

    @TestFactory
    JamalTests<?> testEngine() {
        return JamalYamlTest.factory(
                "TestEngine");
    }

    @TestFactory
    JamalTests<?> testNestedUserDefinedMacros() {
        return JamalYamlTest.factory(
                "nested_ud_macros"
        );
    }

    @TestFactory
    JamalTests<?> testDeepEscape() {
        return JamalYamlTest.factory(
                "TestDeepEscape");
    }

    @TestFactory
    JamalTests<?> testDefer() {
        return JamalYamlTest.factory(
                "TestDefer");
    }

    @TestFactory
    JamalTests<?> testImport() {
        return JamalYamlTest.factory(
                "TestImport");
    }

    @TestFactory
    JamalTests<?> testDefineDefault() {
        return JamalYamlTest.factory(
                "TestDefineDefault");
    }

    @TestFactory
    JamalTests<?> testMacroMacro() {
        return JamalYamlTest.factory(
                "TestMacro");
    }
}
