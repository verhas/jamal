package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestExport {

    @Test
    @DisplayName("Test single export")
    void testSingleExport() throws Exception {
        TestThat.theInput("{#block {@define a=1}{@export a}}{a}").results("1");
    }

    @Test
    @DisplayName("Test multiple exports")
    void testMultipleExport() throws Exception {
        TestThat.theInput("{#block {@define a=1}{@define b=1}{@export a, b}}{a}{b}").results("11");
    }

    @Test
    @DisplayName("Throws exception when the macro exported is not defined")
    void testNotDefined() throws Exception {
        TestThat.theInput("{#block {@define a=1}{@export a, b}}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Throws exception when the macro exported is defined in hiher scope")
    void testDefinedHigher() throws Exception {
        TestThat.theInput("{@define b=1}{#block {@define a=1}{@export a, b}}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Throws exception when the macro exported from top level")
    void testExportFromTopLevel() throws Exception {
        TestThat.theInput("{@define b=1}{@define a=1}{@export a, b}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Throws exception when empty export")
    void testExportEmpty() throws Exception {
        TestThat.theInput("{#block {@export }}").throwsBadSyntax();
    }
}
