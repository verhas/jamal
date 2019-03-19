package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class InputHandlerTest {

    private void assertSplit(String input, String... expected) {
        var in = new Input(input);
        var result = InputHandler.getParts(in);
        Assertions.assertArrayEquals(expected, result);

    }

    @Test
    @DisplayName("input starts with separator, split correctly")
    void splitsInputCorrectlyWhenStartingWithSeparator() {
        assertSplit("/a/b/c/d/e/f/g/h", "a", "b", "c", "d", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("consecutive separators treated as distinct separators")
    void splitsInputCorrectlyWhenStartingWithSeparatorWithConsecutiveSeparators() {
        assertSplit("/a/b/c//e/f/g/h", "a", "b", "c", "", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("first non-whitespace character is treated as separator, leading whitespaces ignored")
    void splitsInputCorrectlyIgnoringStartingWhitespaces() {
        assertSplit("  /a/b/c//e/f/g/h", "a", "b", "c", "", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("input starts with alphanumeric character, treated as separator")
    void splitsInputCorrectlyWithStartingAlphanumericCharacter() {
        assertSplit("    QaQbQcQdQeQfQgQh","a", "b", "c", "d", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("if input ends with separator, the last item is empty")
    void splitsInputCorrectlyWhenEndingWithEmptyInstance() {
        assertSplit("  /a/b/c//e/f/g/h/", "a", "b", "c", "", "e", "f", "g", "h", "");
    }

    @Test
    @DisplayName("input starts with single character regular expression between (`) characters, which is treated as separator")
    void splitsInputCorrectlyStartingWithSingleCharacterRegularExpression() {
        assertSplit("`[0-9]`a3b4c6d7e8f9g0h", "a", "b", "c", "d", "e", "f", "g", "h");
    }

    @Test
    @DisplayName("input starts with multiple character regular expression between (`) characters, which is treated as separator")
    void splitsInputCorrectlyWhenStartingWithMultipleCharacterRegularExpression() {
        assertSplit("`[0-9]w` a0wb1wc1wd2we334wd", "a", "b", "c", "d", "e33", "d");
    }

    @Test
    @DisplayName("input starts with four (`) characters, indicating that the regular expression indicator (`) character is the separator")
    void splitsInputCorrectlyWhenStartingWithRegularExpressionIndicator() {
        assertSplit("```` a`b`c`d`e`f`g`h", " a", "b", "c", "d", "e", "f", "g", "h");
    }
    @Test
    @DisplayName("input starts with regular expression containing special (backslash-escaped character) between (`) characters, which is treated as separator")
    void splitsInputCorrectlyWhenStartingWithRegularExpressionContainingBackslashEscapedCharacter() {
        assertSplit("`\\s*` abcdefgh", "", "", "a", "b", "c", "d", "e", "f", "g", "h", "");
    }

    @Test
    @DisplayName("input starts with regular expression and (`) characters, indicating that the separator is a regular expression that contains the (`) character")
    void splitsInputCorrectlyWhenStartingWithRegularExpressionContainingBackslashEscapedCharacterAndRegularExpressionIndicator() {
        assertSplit("```\\w{2}` a`hiba`wucontal`d0`e`f`g`h", " a", "ba", "contal","`e`f`g`h");
    }
}
