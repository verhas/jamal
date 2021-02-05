package javax0.jamal.test.examples;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestHello {


    // snippet TestHello_1
    @Test
    @DisplayName("Test that the Hello built-in macro works")
    void macroWorks() throws Exception {
        TestThat.theInput(
                "{@hello Peter }\n" +
                "{@hello Paul}\n"
        ).results("Hello, Peter!\nHello, Paul!\n");
    }
    // end snippet
}
