package javax0.jamal.assertions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestAssertEquals {

    @Test
    @DisplayName("Test a simple assert:equals")
    void successfulAssertion() throws Exception {
        TestThat.theInput("{@assert:equals /abba/abba/should be ok/}").results("");
    }

    @Test
    @DisplayName("Test a failing assert:equals")
    void failingAssertion() throws Exception {
        TestThat.theInput("{@assert:equals /abba/ebbe/should fail/}").throwsBadSyntax(".*should fail.*");
    }

    @Test
    @DisplayName("Test a not successful assert:equals")
    void notSuccessfulAssertion() throws Exception {
        TestThat.theInput("{@assert:equals (not) /abba/abba/should fail}").throwsBadSyntax(".*should fail.*");
    }

    @Test
    @DisplayName("Test a not failing assert:equals")
    void notFailingAssertion() throws Exception {
        TestThat.theInput("{@assert:equals (not) /abba/ebbe/should be ok/}").results("");
    }

    @Test
    @DisplayName("Test a space diff only assert:equals w/o trim")
    void failingAssertionNoTrim() throws Exception {
        TestThat.theInput("{@assert:equals /abba/   abba   /should fail/}").throwsBadSyntax(".*should fail.*");
    }

    @Test
    @DisplayName("Test assert:equals failure w/o message")
    void failingAssertionWithoutMessage() throws Exception {
        TestThat.theInput("{@assert:equals /abba/   abba   }").throwsBadSyntax(".*assert:equals has failed 'abba' does not equal '   abba   '.*");
    }

    @Test
    @DisplayName("Test a space diff only assert:equals w/o trim")
    void successAssertionWithTrim() throws Exception {
        TestThat.theInput("{@assert:equals (trim) /abba/   abba   /should fail/}").results("");
    }
}
