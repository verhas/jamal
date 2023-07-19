package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestCollect {

    @Test
    void testLastLineSnipLine() throws Exception {
        TestThat.theInput("" +
                "{@snip:collect from=res:javax0/jamal/snippet/snipline_on_last_line.txt}"
        ).throwsBadSyntax("'snipline kurta' is on the last line of the file.*");
    }

    @Test
    void testSnipLineWithFilter() throws Exception {
        TestThat.theInput("" +
                "{@snip:collect from=res:javax0/jamal/snippet/snipline_with_filter.txt}" +
                "{@snip RestrictedDefineParameters}"
        ).results("RestrictedDefineParameters");
    }

    @Test
    void testCollectWithPrefixPostfix() throws Exception {
        TestThat.theInput("" +
                "{@snip:collect from=res:javax0/jamal/snippet/SnippetSource-1.txt prefix=prefix:: postfix=::postfix}\n" +
                "{@snip prefix::first_snippet::postfix}"
        ).ignoreLineEnding().results("\n" +
                "This is a one line snippet\n");
    }

    @Test
    void testCollectAsciidoc() throws Exception {
        TestThat.theInput("" +
                "{@snip:collect from=res:javax0/jamal/snippet/asciidoctagsnippets.txt asciidoc}" +
                "1. {@snip one}" +
                "2. {@snip two}" +
                "3. {@snip three}" +
                "4. {@snip four}" +
                "5. {@snip five}" +
                ""
        ).ignoreLineEnding().results("" +
                "1. this is one\n" +
                "2. this is two\n" +
                "this is three and two also\n" +
                "3. this is three and two also\n" +
                "4. this is four\n" +
                "this is five and four\n" +
                "5. this is five and four\n" +
                "this is five\n");
    }

    @Test
    void testCollectAsciidocMultiple() throws Exception {
        TestThat.theInput("" +
                "{@snip:collect from=res:javax0/jamal/snippet/asciidoctagsnippets-multiple.txt asciidoc}" +
                "1. {@snip one}" +
                ""
        ).ignoreLineEnding().results("" +
                "1. this is one\n" +
                "one again\n"
        );
    }

    @Test
    void testCollectAsciidocError1() throws Exception {
        TestThat.theInput("" +
                "{@snip:collect from=res:javax0/jamal/snippet/asciidoctagsnippets-e1.txt asciidoc}" +
                "1. {@snip one}" +
                "2. {@snip two}" +
                "3. {@snip three}" +
                "4. {@snip four}" +
                "5. {@snip five}" +
                ""
        ).throwsBadSyntax("Snippet 'one' opened on the line 3 is not closed\\.");
    }

    @Test
    void testCollectAsciidocError2() throws Exception {
        TestThat.theInput("" +
                "{@snip:collect from=res:javax0/jamal/snippet/asciidoctagsnippets-e2.txt asciidoc}" +
                "1. {@snip one}" +
                "2. {@snip two}" +
                "3. {@snip three}" +
                "4. {@snip four}" +
                "5. {@snip five}" +
                ""
        ).throwsBadSyntax("Snippet 'one' is already opened on line 3");
    }

    @Test
    void testJavaCollection() throws Exception {
        TestThat.theInput("{#define javaSnippetCollectors={#j:list {#j:match private void}\\\n" +
                "{#j:identifier (name=snippetName)harvestJava}\\\n" +
                "{#j:anyTill {#j:string (name=snippet)}}\\\n" +
                "}}\\\n" +
                "{@snip:collect from=\"src/main/java\" exclude=\"CounterFormatter\" java}\\\n" +
                "{@snip harvestJava}\n").results("You must specify at least one Java snip\n");
    }

}
