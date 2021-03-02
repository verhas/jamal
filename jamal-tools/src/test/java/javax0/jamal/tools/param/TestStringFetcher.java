package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static javax0.jamal.tools.Input.makeInput;

public class TestStringFetcher {
    private static String string(String s) {
        return "\"" + s + "\"";
    }

    private static String mlstring(String s) {
        return "\"\"\"\n" + s + "\"\"\"";
    }

    @Test
    @DisplayName("Test simple string literals that are syntactically correct")
    void testStringFetchers() throws BadSyntax {
        final var sut = new StringFetcher();
        Assertions.assertEquals("", sut.getString(makeInput(string(""))));
        Assertions.assertEquals("a", sut.getString(makeInput(string("a"))));
        Assertions.assertEquals("\r", sut.getString(makeInput(string("\\r"))));
        Assertions.assertEquals("\n", sut.getString(makeInput(string("\\n"))));
        Assertions.assertEquals("\b", sut.getString(makeInput(string("\\b"))));
        Assertions.assertEquals("\t", sut.getString(makeInput(string("\\t"))));
        Assertions.assertEquals("\f", sut.getString(makeInput(string("\\f"))));
        Assertions.assertEquals("'", sut.getString(makeInput(string("\\'"))));
        Assertions.assertEquals("\"", sut.getString(makeInput(string("\\\""))));
        Assertions.assertEquals("\773", sut.getString(makeInput(string("\\773"))));
        Assertions.assertEquals("\073", sut.getString(makeInput(string("\\073"))));
        Assertions.assertEquals("\079", sut.getString(makeInput(string("\\079"))));
    }

    @Test
    @DisplayName("Test multi line string literals that are syntactically correct")
    void testMultiLineStringFetchers() throws BadSyntax {
        final var sut = new StringFetcher();
        Assertions.assertEquals("\n", sut.getString(makeInput(mlstring(""))));
        Assertions.assertEquals("\na", sut.getString(makeInput(mlstring("a"))));
        Assertions.assertEquals("\n\r", sut.getString(makeInput(mlstring("\\r"))));
        Assertions.assertEquals("\n\n", sut.getString(makeInput(mlstring("\\n"))));
        Assertions.assertEquals("\n\b", sut.getString(makeInput(mlstring("\\b"))));
        Assertions.assertEquals("\n\t", sut.getString(makeInput(mlstring("\\t"))));
        Assertions.assertEquals("\n\f", sut.getString(makeInput(mlstring("\\f"))));
        Assertions.assertEquals("\n'", sut.getString(makeInput(mlstring("\\'"))));
        Assertions.assertEquals("\n\"", sut.getString(makeInput(mlstring("\\\""))));
        Assertions.assertEquals("\n\773", sut.getString(makeInput(mlstring("\\773"))));
        Assertions.assertEquals("\n\073", sut.getString(makeInput(mlstring("\\073"))));
        Assertions.assertEquals("\n\079", sut.getString(makeInput(mlstring("\\079"))));
        Assertions.assertEquals("\na\nb", sut.getString(makeInput(mlstring("a\n\rb"))));
        Assertions.assertEquals("\na\nb", sut.getString(makeInput(mlstring("a\rb"))));
    }

    @Test
    @DisplayName("Test simple string literals that are syntactically incorrect")
    void testBadStringFetchers() {
        final var sut = new StringFetcher();
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput(string("\n"))));
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput(string("\r"))));
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput(string("\\z"))));
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput(string("\\"))));
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput("\"")));
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput("\"\\")));
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput("\"bababa")));
    }

    @Test
    @DisplayName("Test multi line string literals that are syntactically incorrect")
    void testBadMultiLineStringFetchers() {
        final var sut = new StringFetcher();
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput("\"\"\"")));
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput("\"\"\"\"")));
        Assertions.assertThrows(BadSyntax.class, () -> sut.getString(makeInput("\"\"\"\"\"")));
    }
}
