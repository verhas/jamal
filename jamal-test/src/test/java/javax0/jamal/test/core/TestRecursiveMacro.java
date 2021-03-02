package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestRecursiveMacro {

    @Test
    @DisplayName("Test that inifinit recursion throws bad syntax")
    void testInfiniteRecursiveMacro() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestThat.theInput(
            "{@define a={a}}{a}"
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("Test that recursive macros")
    void testRecursiveMacro() throws Exception {
        TestThat.theInput(
            "{@define a($n)={#eval {#if|$n|a{#define m={#eval/JShell $n-1}}{@ident {a {m}}}|{@export m}}{@export m}}}{a 5}"
        ).results("aaaaa");
    }
}
