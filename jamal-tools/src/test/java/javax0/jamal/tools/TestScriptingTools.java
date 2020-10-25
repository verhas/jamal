package javax0.jamal.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static javax0.jamal.tools.ScriptingTools.unescape;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestScriptingTools {

    @Test
    @DisplayName("Unescape removes quote marks from the start and from the end")
    void unescapeQuotes() {
        assertEquals("aaa", unescape("\"aaa\""));
    }

    @Test
    @DisplayName("Unescape converts escaped new line")
    void unescapeNewLine() {
        assertEquals("aa\na", unescape("\"aa\\na\""));
    }



    @Test
    @DisplayName("Unescape converts unescapes new lines")
    void unescapeQuotesInside() {
        assertEquals("aa\"a", unescape("\"aa\\\"a\""));
    }

}
