package javax0.jamal.test.examples;

import javax0.jamal.api.Debugger;
import javax0.jamal.test.tools.junit.IntelliJOnly;
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

    /**
     * This test is used only interactively from IntelliJ to test the debugger UI manually. The execution stops and
     * starts to wait for the debugger. To avoid this when the project is built the code checks that it was invoked from
     * IntelliJ. If the  string "Idea" is present in the stack trace as part of some class name then this is IntelliJ,
     * then we run. Otherwise, the test just returns.
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Used to debug the debugger UI")
    @IntelliJOnly
    void testDebugger() throws Exception {
        System.setProperty(Debugger.JAMAL_DEBUG_SYS, "http:8081?cors=*");
        TestThat.theInput(
            "hahóóó\n".repeat(2) +
                "{@define a=1}{@define b(x)=x2x}{b{a}}"
        ).results("hahóóó\n" +
            "hahóóó\n" +
            "121");
        System.clearProperty(Debugger.JAMAL_DEBUG_SYS);
    }
}
