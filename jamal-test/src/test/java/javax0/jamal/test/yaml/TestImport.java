package javax0.jamal.test.yaml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestImport {

    @Test
    void testImportFromResource() throws Exception {
        TestThat.theInput(
            "{#yaml:define a={@include [verbatim] res:sample.yaml}}{@verbatim a}"
        ).results("" +
            "&id001\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: [1, 2, 3, 5]\n" +
            "q: *id001\n"
        );
    }

    @Test
    void testNormalImportFromResource() throws Exception {
        TestThat.theInput("" +
            "{@define a=wuff wuff}" +
            "{#yaml:define h={@include res:sample.yaml.jam}}" +
            "{@verbatim h}"
        ).results("" +
            "&id001\n" +
            "a: this is wuff wuff\n" +
            "b: this is b\n" +
            "c: [1, 2, 3, 5]\n" +
            "q: *id001\n"
        );
    }
}
