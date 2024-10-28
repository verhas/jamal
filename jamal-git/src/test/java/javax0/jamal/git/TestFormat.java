package javax0.jamal.git;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestFormat {

    @Test
    void testTimeFormat() throws Exception {
        final var time = "1620000000";
        final var format = "{@git:format (time=" + time + ") yyyy-MM-dd hh:mm:ss}";
        final var expected = "2021-05-03 12:00:00";
        TestThat.theInput(format).results(expected);
    }

}
