package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestSnipLineAndFIle {
    final static String RESOURCE_ROOT = new File(TestSnippets.class.getClassLoader().getResource("javax0/jamal/snippet/test3.jam").getFile()).getParent();

    @Test
    @DisplayName("You can query the line number of the snippet from the file it was collected from")
    void testSnipLine() throws Exception {
        TestThat.theInput("{@include " + RESOURCE_ROOT + "/test5.jam}" +
                "{@snip:line first_snippet}"
        ).results("4");
    }

    @Test
    @DisplayName("You can query the file name of the snippet from it was collected from")
    void testSnipFile() throws Exception {
        String s;
        s = TestThat.theInput("{@include " + RESOURCE_ROOT + "/test5.jam}" +
                "{@snip:file first_snippet}"
        ).results();
        Assertions.assertTrue(s.endsWith("SnippetSource-1.txt"));
    }
}
