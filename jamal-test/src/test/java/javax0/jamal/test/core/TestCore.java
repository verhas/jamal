package javax0.jamal.test.core;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

public class TestCore {

    @TestFactory
    JamalTests<?> testAllCoreMacros1() {
        return JamalYamlTest.factory(
            "TestFor");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacros2() {
        return JamalYamlTest.factory(
            "TestSamples");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacros3() {
        return JamalYamlTest.factory(
            "TestDefine");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacros4() {
        return JamalYamlTest.factory(
            "TestBlock");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacros5() {
        return JamalYamlTest.factory(
            "TestComment");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacros6() {
        return JamalYamlTest.factory(
            "TestEscape");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacros7() {
        return JamalYamlTest.factory(
            "TestEval");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacros8() {
        return JamalYamlTest.factory(
            "TestExport");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacros9() {
        return JamalYamlTest.factory(
            "TestIdent");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosA() {
        return JamalYamlTest.factory(
            "TestIf");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosB() {
        return JamalYamlTest.factory(
            "TestJShell");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosD() {
        return JamalYamlTest.factory(
            "TestOptions");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosE() {
        return JamalYamlTest.factory(
            "TestRecursiveMacro");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosF() {
        return JamalYamlTest.factory(
            "TestRequire");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosG() {
        return JamalYamlTest.factory(
            "TestTry");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosH() {
        return JamalYamlTest.factory(
            "TestUndefine");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosI() {
        return JamalYamlTest.factory(
            "TestUse");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosJ() {
        return JamalYamlTest.factory(
            "TestSep");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosK() {
        return JamalYamlTest.factory(
            "TestEngine");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosL() {
        return JamalYamlTest.factory(
            "nested_ud_macros"
        );
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosM() {
        return JamalYamlTest.factory(
            "TestDeepEscape");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosN() {
        return JamalYamlTest.factory(
            "TestDefer");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosO() {
        return JamalYamlTest.factory(
            "TestImport");
    }

    @TestFactory
    JamalTests<?> testAllCoreMacrosP() {
        return JamalYamlTest.factory(
            "TestDefineDefault");
    }
}
