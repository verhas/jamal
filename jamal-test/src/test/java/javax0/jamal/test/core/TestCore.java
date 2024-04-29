package javax0.jamal.test.core;

import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    JamalTests<?> testError() {
        return JamalYamlTest.factory(
                "TestError");
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

    /**
     * This test runs only when jamal version is not 2.x anymore. In this case, the parop 'type' of the macro 'macro'
     * has to be removed. If this test fails, then check first that the version is really 3.0.0 or later.
     * It may also fail when you run this test from IntelliJ IDE, because the build process does not filter the
     * version.properties file. In this case, just do not run the test. It is there to warn the developers that the
     * documentation promises that the parop 'type' will be removed in the release 3.0.0.
     *
     * @throws Exception generally never but the underlying methods declare it
     */
    @Test
    @DisplayName("Macro parop 'type' is deprecated and has to be removed in version 3 and later")
    void testDeprecation() throws Exception {
        final var v = Processor.jamalVersionString();
        if (!v.startsWith("2")) {
            TestThat.theInput("{@define a=yayy}{#ident {@define a=value of a}{@macro [global type=\"user defined\"]a}}").throwsBadSyntax();
        }
    }

}
