package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestSnippet {

    @Test
    @DisplayName("Snippets can be defined and then used in snip macro")
    void testSnippetDefinition() throws Exception {
        TestThat.theInput("{@snip:define snippet1=    \n" +
            "this is the content of the snippet\n" +
            "this is the content of the snippet\n" +
            "this is the content of the snippet\n" +
            "this is the content of the snippet\n" +
            "}{@snip snippet1 this is ignored\n" +
            "a way ignored}").results(
            "this is the content of the snippet\n" +
                "this is the content of the snippet\n" +
                "this is the content of the snippet\n" +
                "this is the content of the snippet\n");
    }

    @Test
    @DisplayName("Snippets can be used partially with regex")
    void testSnippetPartUse() throws Exception {
        TestThat.theInput("{@snip:define snippet=abra kadabra\n" +
            "this is the content of the snippet}{@snip snippet /abra\\s*(.*)/}")
            .results("kadabra");
    }

    @Test
    @DisplayName("Empty snippets can be used partially with regex")
    void testEmptySnippetPartUse() throws Exception {
        TestThat.theInput("{@snip:define snippet=}{@snip snippet /\\s*(.*)/}")
            .results("");
    }

    @Test
    @DisplayName("Throws exception when a regular expression is not found")
    void testRegexNotFound() throws Exception {
        TestThat.theInput("{@snip:define snippet=ziba}{@snip snippet /dsd\\s*(.*)/}")
            .throwsBadSyntax();
    }
    @Test
    @DisplayName("Throws exception when a regular expression is erroneous")
    void testRegexBad() throws Exception {
        TestThat.theInput("{@snip:define snippet=ziba}{@snip snippet /dsd\\s*(.*/}")
            .throwsBadSyntax();
    }
    @Test
    @DisplayName("Snippet with bad regex")
    void testRegexNoCapturingGroup() throws Exception {
        TestThat.theInput("{@snip:define snippet=z}{@snip snippet /\\s*.*/}")
            .throwsBadSyntax();
    }

    @Test
    @DisplayName("Throws exception when the regular expression is nor closed after the snippet")
    void testSnippetPartUseBad() throws Exception {
        TestThat.theInput("{@snip:define snippet=abra kadabra\n" +
            "this is the content of the snippet}{@snip snippet /abra\\s*(.*)}")
            .throwsBadSyntax();
    }

    @Test
    void testSnippetReDefinition() throws Exception {
        TestThat.theInput("{@snip:define snippet1=.}{@snip:define snippet1=.}").throwsBadSyntax();
    }
}
