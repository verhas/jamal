package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestCollect {

    @Test
    void testLastLineSnipLine()throws Exception{
        TestThat.theInput(""+
            "{@snip:collect from=res:javax0/jamal/snippet/snipline_on_last_line.txt}"
        ).throwsBadSyntax("'snipline kurta' is on the last line of the file.*");
    }

    @Test
    void testCollectWithPrefixPostfix()throws Exception{
        TestThat.theInput(""+
            "{@snip:collect from=res:javax0/jamal/snippet/SnippetSource-1.txt prefix=prefix:: postfix=::postfix}\n" +
                "{@snip prefix::first_snippet::postfix}"
        ).results("\n" +
                "This is a one line snippet\n");
    }
}
