package javax0.jamal.yaml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestDump {

    @Test
    void testDump() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\na: a\nb: b}\n" +
            "{@yaml:dump a to target/dumped.yaml}\n" +
            "{#yaml:define b={@include [verbatim]target/dumped.yaml}}\n" +
            "{@verbatim b}"
        ).results("" +
            "\n\n\n" +
            "{a: a, b: b}\n"
        );
    }

    @Test
    void testDumpResolved() throws Exception {
        TestThat.theInput("" +
            "{#yaml:define a=\na: a\nb: {@yaml:ref q}}\n" +
            "{#yaml:define q=\na: a\nq: qka}\n" +
            "{@yaml:dump a to target/qka.yaml}\n" +
            "{#yaml:define b={@include [verbatim]target/qka.yaml}}\n" +
            "{@yaml:output b}"
        ).results("" +
            "a: a\n" +
            "b: {a: a, q: qka}\n"
        );
    }
}
