package javax0.jamal.assertions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestAssertEmpty {

    @Test
    @DisplayName("Test non empty string assertion")
    void testNonEmptyStringFail() throws Exception {
        TestThat.theInput("{@assert:empty /abba/should fail}").throwsBadSyntax(".*should fail.*");
    }

    @Test
    @DisplayName("Test empty string assertion")
    void testEmptyStringFail() throws Exception {
        TestThat.theInput("{@assert:empty (not) //should fail}").throwsBadSyntax(".*should fail.*");
    }

    @Test
    @DisplayName("Test non empty string assertion with default message")
    void testNonEmptyStringFailDefaultMessage() throws Exception {
        TestThat.theInput("{@assert:empty /abba/}").throwsBadSyntax(".*assert:empty has failed 'abba' is not empty.*");
    }

    @Test
    @DisplayName("Test empty string assertion with default message")
    void testEmptyStringFailDefaultMessage() throws Exception {
        TestThat.theInput("{@assert:empty (not) //}").throwsBadSyntax(".*assert:empty has failed value is empty.*");
    }

    @Test
    @DisplayName("Test empty string assertion")
    void testEmptyString() throws Exception {
        TestThat.theInput("{@assert:empty //should fail}").results("");
    }

    @Test
    @DisplayName("Test non empty string assertion")
    void testNonEmptyString() throws Exception {
        TestThat.theInput("{@assert:empty(not) /abba/should fail}").results("");
    }
}
