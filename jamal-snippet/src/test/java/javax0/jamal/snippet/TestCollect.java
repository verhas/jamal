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
}
