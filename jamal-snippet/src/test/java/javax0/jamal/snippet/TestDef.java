package javax0.jamal.snippet;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestDef {


    @Test
    void testDef() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{@def id=1}{id}")
                .atPosition(root + "/jamal-snippet/src/test/resources/README.adoc.jam", 1, 1)
                .results("11");
    }

    @Test
    void testDefWrong1() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{@def id:6=1}{id:6}")
                .atPosition(root + "/jamal-snippet/src/test/resources/README.adoc.jam", 1, 1)
                .throwsBadSyntax("The id in the def macro cannot contain ':'");
    }

    @Test
    void testDefWrong2() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{@def id/1}{id}")
                .atPosition(root + "/jamal-snippet/src/test/resources/README.adoc.jam", 1, 1)
                .throwsBadSyntax("Missing = after the id in the def macro");
    }

}
