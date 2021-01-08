package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestSnippets {


    @Test
    void testSimpleCase() throws InvocationTargetException, NoSuchMethodException, InstantiationException, BadSyntax, IllegalAccessException {
        TestThat.theInput("{@include src/test/resources/javax0/jamal/snippet/test1.jam}")
            .results("\n" +
                "First snippet This is a one line snippet\n" +
                "\n" +
                "2. snippet This is a multi line snippet,\n" +
                "the second\n" +
                "  snippet, still from SnippetSource-1.txt\n" +
                "\n" +
                "\n" +
                "Next file\n" +
                "\n" +
                "First snippet This is a one line snippet\n" +
                "\n" +
                "Second snippet This is a multi line snippet,\n" +
                "the second\n" +
                "  snippet, still from SnippetSource-1.txt\n" +
                "\n" +
                "\n" +
                "and this is the end");
    }
}
