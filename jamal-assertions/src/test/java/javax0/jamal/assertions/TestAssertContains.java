package javax0.jamal.assertions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestAssertContains {

    @Test
    @DisplayName("Test a simple assert:contains")
    void successfulAssertion() throws Exception {
        TestThat.theInput("{@assert:contains /abbaba/abba/should be ok/}").results("");
    }

    @Test
    @DisplayName("Test a failing assert:contains")
    void failingAssertion() throws Exception {
        TestThat.theInput("{@assert:contains /abba/ebbe/should fail/}").throwsBadSyntax(".*should fail.*");
    }

    @Test
    @DisplayName("Test a not successful assert:contains")
    void notSuccessfulAssertion() throws Exception {
        TestThat.theInput("{@assert:contains (not) /abba/abba/should fail}").throwsBadSyntax(".*should fail.*");
    }

    @Test
    @DisplayName("Test a not failing assert:contains")
    void notFailingAssertion() throws Exception {
        TestThat.theInput("{@assert:contains (not) /abba/ebbe/should be ok/}").results("");
    }

    @Test
    @DisplayName("Test a space diff only assert:contains w/o trim")
    void failingAssertionNoTrim() throws Exception {
        TestThat.theInput("{@assert:contains /abba/   abba   /should fail/}").throwsBadSyntax(".*should fail.*");
    }

    @Test
    @DisplayName("Test assert:contains failure w/o message")
    void failingAssertionWithoutMessage() throws Exception {
        TestThat.theInput("{@assert:contains /abba/   abba   }").throwsBadSyntax(".*assert:contains has failed 'abba' does not contain '   abba   '.*");
    }

    @Test
    @DisplayName("Test a space diff only assert:contains w/o trim")
    void successAssertionWithTrim() throws Exception {
        TestThat.theInput("{@assert:contains (trim) /abba/   abba   /should fail/}").results("");
    }
}
