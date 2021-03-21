package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestClear {

    @Test
    @DisplayName("Clear clears all the snippets from the snippet store")
    void testClear()throws Exception{
        TestThat.theInput("{@snip:define snippet1=1}{@snip:clear}{@snip snippet1}").throwsBadSyntax();
    }
}
