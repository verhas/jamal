package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestPos {

    @Test
    @DisplayName("Simple pos test")
    void testSimpleSample() throws Exception {
        TestThat.theInput("{@pos.file}:{@pos.line}:{@pos.column}").atPosition("wupsy",666,1937).results("wupsy:666:1966");
    }

}
