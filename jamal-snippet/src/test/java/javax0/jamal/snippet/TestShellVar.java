package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestShellVar {

    @Test
    @DisplayName("simple shell variable replacement")
    void testSimpleReplacement() throws Exception {
        TestThat.theInput("" +
                "{@define A=this is the replacement of the shell variable}" +
                "{@shell:var $A}"
        ).results("this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter")
    void testSimpleReplacementWithParameters() throws Exception {
        TestThat.theInput("" +
                "{@define A=this is the replacement of the shell variable}" +
                "{@shell:var (variables=\"A=ops\")$A}"
        ).results("ops");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter inside the parameter name")
    void testSimpleReplacementWithParametersInName() throws Exception {
        TestThat.theInput("" +
                "{@define A=this is the replacement of the shell variable}" +
                "{@define B=A}" +
                "{@shell:var ${$B}}"
        ).results("this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter inside the parameter value")
    void testSimpleReplacementWithParametersInValue() throws Exception {
        TestThat.theInput("" +
                "{@define A=this is the replacement of the shell variable}" +
                "{@define B=$A}" +
                "{@shell:var $B}"
        ).results("this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("simple shell variable replacement with parameter inside the parameter value as well as in the name")
    void testSimpleReplacementWithParametersInValueAndInName() throws Exception {
        TestThat.theInput("" +
                "{@define this=that or this}" +
                "{@define A=$this is the replacement of the shell variable}" +
                "{@define B=$A}" +
                "{@shell:var ${B}}"
        ).results("that or this is the replacement of the shell variable");
    }

    @Test
    @DisplayName("too deep recursion")
    void testTooDeepRecursion() throws Exception {
        TestThat.theInput("" +
                "{@define recursion=$recursion}" +
                "{@shell:var $recursion}"
        ).throwsBadSyntax("Too deep recursion in shell variable substitution");
    }
}
