package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestSnippet {

    @Test
    void testSnippetDefinition() throws Exception {
        TestThat.theInput("{@snippet snippet1=    \n" +
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
    void testSnippetReDefinition() throws Exception {
        TestThat.theInput("{@snippet snippet1=.}{@snippet snippet1=.}").throwsBadSyntax();
    }
}
