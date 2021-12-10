package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;
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
    @DisplayName("Snippets can be checked for change")
    void testSnippetCheck1() throws Exception {
        TestThat.theInput("{@snip:define snippet1=    \n" +
                "this is the content of the snippet}" +
                "{@snip:check hash=b0a2c1555ec779acfdfd3d09190921adcd4a8efdbe536993cf48112a7d47ee01 id=snippet1}")
            .results("");
    }

    private static final String JAMAL_SNIPPET_CHECK = "jamal.snippet.check";

    @Test
    @DisplayName("Snippets check fails even for one character change")
    void testSnippetCheck2() throws Exception {
        final var save = System.getProperty(JAMAL_SNIPPET_CHECK);
        System.setProperty(JAMAL_SNIPPET_CHECK, "true");
        TestThat.theInput("{@snip:define snippet1=    \n" +
                "thjs is the content of the snippet}" +
                "{@snip:check hash=47ee01 id=snippet1}")
            .throwsBadSyntax(".*The id\\(snippet1\\) hash is '[a-f0-9\\.]{71}' does not contain '47ee01'\\..*");
        if (save == null) {
            System.clearProperty(JAMAL_SNIPPET_CHECK);
        } else {
            System.setProperty(JAMAL_SNIPPET_CHECK, save);
        }
    }

    @Test
    @DisplayName("Snippets check works with any postfix that is at least 6 characters")
    void testSnippetCheck3() throws Exception {
        TestThat.theInput("{@snip:define snippet1=    \n" +
                "this is the content of the snippet}" +
                "{@snip:check hash=47ee01 id=snippet1}")
            .results("");
    }

    @Test
    @DisplayName("Snippets check fails with zero length postfix")
    void testSnippetCheck4() throws Exception {
        final var save = System.getProperty(JAMAL_SNIPPET_CHECK);
        System.setProperty(JAMAL_SNIPPET_CHECK, "true");
        TestThat.theInput("{@snip:define snippet1=    \n" +
                "this is the content of the snippet}" +
                "{@snip:check hash=\"\" id=snippet1}")
            .throwsBadSyntax(".*The id\\(snippet1\\) hash is '[a-f0-9\\.]{71}'\\..*");
        if (save == null) {
            System.clearProperty(JAMAL_SNIPPET_CHECK);
        } else {
            System.setProperty(JAMAL_SNIPPET_CHECK, save);
        }
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
    @DisplayName("Snippet definition with no value")
    void testSnipNoValue() throws Exception {
        TestThat.theInput("{@snip:define snippet}")
            .throwsBadSyntax();
    }

    @Test
    @DisplayName("Snippet cannot be redefined")
    void testNoRedefine() throws Exception {
        TestThat.theInput("{@snip:define a=1}{@snip:define a=1}")
            .throwsBadSyntax();
    }

    @Test
    @DisplayName("Accidental snippet can be redefined")
    void testRedefineErroneous() throws Exception {
        final var test = TestThat.theInput("{@snip:define a=1}{@snip a}");
        // simulate collection of accidental snippet, which is not closed in a file
        SnippetStore.getInstance(test.getProcessor())
            .snippet("a", "1", new Position("a"), new BadSyntaxAt("test bad syntax exception", null));
        // executing the code will redefine the snippet
        test.results("1");
    }

    @Test
    @DisplayName("Multiple accidental snippets cannot be used")
    void testRedefineErroneousWithErroneous() throws Exception {
        final var test = TestThat.theInput("{@snip a}");
        // simulate collection of accidental snippet, which is not closed in a file
        SnippetStore.getInstance(test.getProcessor())
            .snippet("a", "1", new Position("a"), new BadSyntaxAt("test bad syntax exception", null));
        SnippetStore.getInstance(test.getProcessor())
            .snippet("a", "2", new Position("a"), new BadSyntaxAt("test bad syntax exception", null));
        // executing the code will redefine the snippet
        test.throwsBadSyntax();
    }

    @Test
    @DisplayName("Accidental snippet does not redefine normal snippet")
    void testErroneousDoesNotRedefine() throws Exception {
        final var test = TestThat.theInput("{@snip a}");
        // simulate normally collected snippet, no error during collection
        SnippetStore.getInstance(test.getProcessor())
            .snippet("a", "1", new Position("a"));
        // simulate collection of accidental snippet, which is not closed in a file
        SnippetStore.getInstance(test.getProcessor())
            .snippet("a", "3", new Position("a"), new BadSyntaxAt("test bad syntax exception", new Position("")));
        test.results("1");
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
