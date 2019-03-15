package javax0.jamal.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InputHandlerTest {

    private void assertSplit(String input, String... expected) {
        var in = new Input(input);
        var result = InputHandler.getParts(in);
        Assertions.assertArrayEquals(expected, result);

    }

    @Test
    void splitsInputCorrectly() {
        assertSplit("/a/b/c/d/e/f/g/h/", "a", "b", "c", "d", "e", "f", "g", "h", "");
        assertSplit("/a/b/c//e/f/g/h/", "a", "b", "c", "", "e", "f", "g", "h", "");
        assertSplit("  /a/b/c//e/f/g/h/", "a", "b", "c", "", "e", "f", "g", "h", "");
        assertSplit("  /a/b/c//e/f/g/h", "a", "b", "c", "", "e", "f", "g", "h");

        assertSplit("`\\s*` abcdefgh", "", "", "a", "b", "c", "d", "e", "f", "g", "h", "");
        assertSplit("```` a`b`c`d`e`f`g`h", " a", "b", "c", "d", "e", "f", "g", "h");
        assertSplit("```\\w{2}` a`hiba`wucontal`d0`e`f`g`h", " a", "ba", "contal","`e`f`g`h");
    }
}
