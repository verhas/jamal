package javax0.jamal.snippet;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestEval {

    @Test
    void testBothEvals() throws Exception {
        final var root = DocumentConverter.getRoot();
        DocumentConverter.convert(root + "/jamal-snippet/src/test/resources/snipeval/test.jam");
        final var out = Paths.get(root + "/jamal-snippet/src/test/resources/snipeval/test");
        final var result = String.join("\n", Files.readAllLines(out));
        Assertions.assertEquals("" +
                "This is included when calling core eval.\n" +
                "\n" +
                "This is included when calling snip:eval.\n" +
                "\n" +
                "This is included when calling snip:eval.\n", result);
        Files.delete(out);
    }

    @Test
    void testDefine() throws Exception {
        final var root = DocumentConverter.getRoot();
        DocumentConverter.convert(root + "/jamal-snippet/src/test/resources/snipeval/test2fordefine.jam");
        final var out = Paths.get(root + "/jamal-snippet/src/test/resources/snipeval/test2fordefine");
        final var result = String.join("\n", Files.readAllLines(out));
        Assertions.assertEquals("" +
                "This is included when calling core eval.\n" +
                "This is included when calling snip:eval.", result);
        Files.delete(out);
    }
}
