package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUserDefinedMacro {

    @Test
    @DisplayName("throws exception when an argument appears more than once")
    public void testRepeatedArguments() {
        Assertions.assertThrows(BadSyntax.class, () ->
            new UserDefinedMacro("xx", "", "a", "b", "a", "d"));
    }

    @Test
    @DisplayName("throws exception when an argument is prefix of a later argument")
    public void testPrefixArguments1() {
        Assertions.assertThrows(BadSyntax.class, () ->
            new UserDefinedMacro("xx", "", "a", "b", "alma", "d"));
    }

    @Test
    @DisplayName("throws exception when an argument is prefix of an earlier argument")
    public void testPrefixArguments2() {
        Assertions.assertThrows(BadSyntax.class, () ->
            new UserDefinedMacro("xx", "", "alma", "b", "a", "d"));
    }


    @Test
    @DisplayName("creates the macro object when there are no arguments")
    public void testNoArguments() throws BadSyntax {
        final var sut = new UserDefinedMacro("xx", "");
    }

    @Test
    @DisplayName("replaces arguments with actual values")
    public void testReplaces() throws BadSyntax {
        final var sut = new UserDefinedMacro("xx", "a{b}c{d}", "a", "b", "c", "d");
        final var result = sut.evaluate("bbb", "ccc", "ddd", "aaa");
        Assertions.assertEquals("bbb{ccc}ddd{aaa}", result);
    }


}
