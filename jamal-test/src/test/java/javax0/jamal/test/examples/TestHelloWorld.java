package javax0.jamal.test.examples;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestHelloWorld {


    // snippet TestHelloWorld_1
    @Test
    @DisplayName("Test that the HelloWorld built-in macro works")
    void macroWorks() throws Exception {
        TestThat.theInput(
            "{@use javax0.jamal.test.examples.HelloWorld}" +
                "{@helloworld}"
        ).results("Hello, World!");
    }
    // end snippet

    // snippet TestHelloWorld_2
    @Test
    @DisplayName("Test that the HelloWorld built-in macro is registered")
    void macroRegisteredGLobal() throws Exception {
        TestThat.theInput(
            "{@helloworld}"
        ).results("Hello, World!");
    }

    // end snippet
    // snippet TestHelloWorld_3
    @Test
    @DisplayName("Test that the HelloWorld built-in macro works")
    void macroIgnoresInput() throws Exception {
        TestThat.theInput(
            "{@helloworld the input is totally ignored}"
        ).results("Hello, World!");
    }
    // end snippet
}
