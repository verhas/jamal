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
        Assertions.assertEquals("", StringFetcher.getString(makeInput(string(""))));
        Assertions.assertEquals("a", StringFetcher.getString(makeInput(string("a"))));
        Assertions.assertEquals("\r", StringFetcher.getString(makeInput(string("\\r"))));
        Assertions.assertEquals("\n", StringFetcher.getString(makeInput(string("\\n"))));
        Assertions.assertEquals("\b", StringFetcher.getString(makeInput(string("\\b"))));
        Assertions.assertEquals("\t", StringFetcher.getString(makeInput(string("\\t"))));
        Assertions.assertEquals("\f", StringFetcher.getString(makeInput(string("\\f"))));
        Assertions.assertEquals("'", StringFetcher.getString(makeInput(string("\\'"))));
        Assertions.assertEquals("\"", StringFetcher.getString(makeInput(string("\\\""))));
        Assertions.assertEquals("\773", StringFetcher.getString(makeInput(string("\\773"))));
        Assertions.assertEquals("\073", StringFetcher.getString(makeInput(string("\\073"))));
        Assertions.assertEquals("\079", StringFetcher.getString(makeInput(string("\\079"))));
    }

    @Test
    @DisplayName("Test multi line string literals that are syntactically correct")
    void testMultiLineStringFetchers() throws BadSyntax {
        Assertions.assertEquals("\n", StringFetcher.getString(makeInput(mlstring(""))));
        Assertions.assertEquals("\na", StringFetcher.getString(makeInput(mlstring("a"))));
        Assertions.assertEquals("\n\r", StringFetcher.getString(makeInput(mlstring("\\r"))));
        Assertions.assertEquals("\n\n", StringFetcher.getString(makeInput(mlstring("\\n"))));
        Assertions.assertEquals("\n\b", StringFetcher.getString(makeInput(mlstring("\\b"))));
        Assertions.assertEquals("\n\t", StringFetcher.getString(makeInput(mlstring("\\t"))));
        Assertions.assertEquals("\n\f", StringFetcher.getString(makeInput(mlstring("\\f"))));
        Assertions.assertEquals("\n'", StringFetcher.getString(makeInput(mlstring("\\'"))));
        Assertions.assertEquals("\n\"", StringFetcher.getString(makeInput(mlstring("\\\""))));
        Assertions.assertEquals("\n\773", StringFetcher.getString(makeInput(mlstring("\\773"))));
        Assertions.assertEquals("\n\073", StringFetcher.getString(makeInput(mlstring("\\073"))));
        Assertions.assertEquals("\n\079", StringFetcher.getString(makeInput(mlstring("\\079"))));
        Assertions.assertEquals("\na\nb", StringFetcher.getString(makeInput(mlstring("a\n\rb"))));
        Assertions.assertEquals("\na\nb", StringFetcher.getString(makeInput(mlstring("a\rb"))));
    }

    @Test
    @DisplayName("Test simple string literals that are syntactically incorrect")
    void testBadStringFetchers() {
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput(string("\n"))));
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput(string("\r"))));
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput(string("\\z"))));
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput(string("\\"))));
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput("\"")));
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput("\"\\")));
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput("\"bababa")));
    }

    @Test
    @DisplayName("Test multi line string literals that are syntactically incorrect")
    void testBadMultiLineStringFetchers() {
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput("\"\"\"")));
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput("\"\"\"\"")));
        Assertions.assertThrows(BadSyntax.class, () -> StringFetcher.getString(makeInput("\"\"\"\"\"")));
    }
}
