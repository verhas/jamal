package javax0.jamal.tools;

import javax0.jamal.api.BadSyntaxAt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestInputHandler {

    private void assertSplit(String input, String... expected) {
        var in = new Input(input);
        final String[] result;
        result = InputHandler.getParts(in);

        Assertions.assertArrayEquals(expected, result);

    }

    @Test
    @DisplayName("input starts with separator, split correctly")
    void splitsInputCorrectlyWhenStartingWithSeparator() {
        assertSplit("/a/b/c/d/e/f/g/h", "a", "b", "c", "d", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("input starts with regex separator, split correctly")
    void splitsInputCorrectlyWhenStartingWithRegexSeparator() {
        assertSplit("`\\n`\na\nb\nc\nd\ne\nf\ng\nh", "a", "b", "c", "d", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("first character treated as separator with leading whitespaces ignored")
    void splitsInputCorrectlyIgnoringStartingWhitespaces() {
        assertSplit("  /a/b/c//e/f/g/h", "a", "b", "c", "", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("consecutive separators treated as distinct separators")
    void splitsInputCorrectlyWhenStartingWithSeparatorWithConsecutiveSeparators() {
        assertSplit("/a/b/c//e/f/g/h", "a", "b", "c", "", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("if input ends with separator, the last item is empty")
    void splitsInputCorrectlyWhenEndingWithEmptyInstance() {
        assertSplit("  /a/b/c//e/f/g/h/", "a", "b", "c", "", "e", "f", "g", "h", "");
    }

    @Test
    @DisplayName("input starts with regular expression")
    void splitsInputCorrectlyStartingWithRegularExpression() {
        assertSplit("`[0-9]`a3b4c6d7e8f9g0h", "a", "b", "c", "d", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("separator is the backtick character")
    void splitsInputCorrectlyWithBacktickAsSeparator() {
        assertSplit("```` a`b`c`d`e`f`g`h", " a", "b", "c", "d", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("separator is regular expression containing special (backslash-escaped) character")
    void splitsInputCorrectlyWithRegularExpressionContainingSpecialCharacter() {
        assertSplit("`\\s*`  abcdefgh", "", "a", "b", "c", "d", "e", "f", "g", "h", "");
    }

    @Test
    @DisplayName("separator includes backtick AND regular expression")
    void splitsInputCorrectlyWhenStartingWithComplexRegularExpression() {
        assertSplit("```\\w{2}` a`hiba`wucontal`d0`e`f`g`h", " a", "ba", "contal", "`e`f`g`h");
    }

    @Test
    @DisplayName("Finds the prefix that starts the sequence")
    void testStartsWithFindsString() {
        final var in = new Input("prefix at the start of the string");
        Assertions.assertEquals(1, InputHandler.startsWith(in, "profix", "prefix", "bufix"));
    }

    @Test
    @DisplayName("Finds the prefix that starts the sequence, when it is the first one")
    void testStartsWithFindsTheFirstString() {
        final var in = new Input("prefix at the start of the string");
        Assertions.assertEquals(0, InputHandler.startsWith(in, "prefix", "profix", "bufix"));
    }

    @Test
    @DisplayName("Finds the prefix that starts the sequence, when it is the last one")
    void testStartsWithFindsTheLastString() {
        final var in = new Input("prefix at the start of the string");
        Assertions.assertEquals(2, InputHandler.startsWith(in, "profix", "bufix", "prefix"));
    }

    @Test
    @DisplayName("Finds the prefix that starts the sequence even when some strings are longer than the input")
    void testStartsWithFindsStringWhenSomeStringAreLong() {
        final var in = new Input("prefix s");
        Assertions.assertEquals(2, InputHandler.startsWith(in, "profix asa", "bufix", "prefix"));
    }

    @Test
    @DisplayName("Finds the prefix that starts the sequence even when some strings are longer than the input")
    void testStartsWithDetectsNotFound() {
        final var in = new Input("prrrrefix s");
        Assertions.assertEquals(-1, InputHandler.startsWith(in, "profix asa ", "bufix aaaaaa a a a a a  a a a ", "prefix"));
    }
}
