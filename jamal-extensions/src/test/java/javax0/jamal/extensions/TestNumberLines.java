package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestNumberLines {

    @Test
    public void testCamelLowerCase() throws Exception {
        var numberLines = TestThat.forMacro(NumberLines.class);
        numberLines.fromInput("line 1\n" +
            "line 2\n" +
            "line 3\n").results("1. line 1\n" +
            "2. line 2\n" +
            "3. line 3\n");
    }
}
