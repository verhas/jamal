package javax0.jamal.test.yaml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestGet {

    @Test
    void testGet() throws Exception {
        TestThat.theInput(""+
            "{@yaml:define a=\n" +
            "a: alma\n" +
            "b:\n" +
            "  c: 3\n" +
            "  d:\n" +
            "    - 1\n" +
            "    - 2\n" +
            "    - q:\n" +
            "        h: deep h}" +
            "{@yaml:get (from=a) b.d[2].q.h}"
        ).results("deep h");
    }
}
