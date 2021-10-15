package javax0.jamal.assertions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestNumericAsserts {

    @Test
    @DisplayName("Test numeric inequality")
    void testIntEqualsFail() throws Exception {
        TestThat.theInput("{@assert:intEquals /3/4}")
            .throwsBadSyntax(".*assert:intEquals has failed '3' does not equal '4'.*");
    }

    @Test
    @DisplayName("Test numeric equality")
    void testIntEquals() throws Exception {
        TestThat.theInput("{@assert:intEquals /3/+3}").results("");
        TestThat.theInput("{@assert:equals /3/+3}")
            .throwsBadSyntax(".*assert:equals has failed '3' does not equal '\\+3'.*");
    }

    @Test
    @DisplayName("Test numeric less, greater and xOrEqual")
    void testLess() throws Exception {
        TestThat.theInput("{@assert:less /2/3}").results("");
        TestThat.theInput("{@assert:lessOrEqual /2/3}").results("");
        TestThat.theInput("{@assert:lessOrEquals /2/2}").results("");

        TestThat.theInput("{@assert:greater /4/3}").results("");
        TestThat.theInput("{@assert:greaterOrEqual /4/3}").results("");
        TestThat.theInput("{@assert:greaterOrEqual /4/4}").results("");

        TestThat.theInput("{@assert:less /4/3}")
            .throwsBadSyntax("assert:less has failed '4' is not less '3'.*");
        TestThat.theInput("{@assert:lessOrEqual /4/3}")
            .throwsBadSyntax("assert:lessOrEqual has failed '4' is not less or equal '3'.*");

        TestThat.theInput("{@assert:greater /1/3}")
            .throwsBadSyntax("assert:greater has failed '1' is not greater '3'.*");
        TestThat.theInput("{@assert:greaterOrEquals /1/3}")
            .throwsBadSyntax("assert:greaterOrEqual has failed '1' is not greater or equal '3'.*");
    }

    @Test
    @DisplayName("Test format error, parameter is not an integer")
    void formatError() throws Exception {
        TestThat.theInput("{@assert:intEquals /3/this is not a number}")
            .throwsBadSyntax("The parameter in assert:intEquals is not a well formatted integer: 'this is not a number'.*");
        TestThat.theInput("{@assert:intEquals /this is not a number/3}")
            .throwsBadSyntax("The parameter in assert:intEquals is not a well formatted integer: 'this is not a number'.*");
    }

    @Test
    @DisplayName("Test format error not enough arguments")
    void formatErrorNoEnoughAruments() throws Exception {
        TestThat.theInput("{@assert:intEquals 42}")
            .throwsBadSyntax("assert:intEquals needs at least 2 arguments.*");
    }

    @Test
    @DisplayName("Test numericity and fail")
    void numericFail() throws Exception {
        TestThat.theInput("{@assert:numeric /2.3a/this is not a number}").throwsBadSyntax("this is not a number.*");
    }

    @Test
    @DisplayName("Test numeric okay")
    void numericOK() throws Exception {
        TestThat.theInput("{@assert:numeric /2.3/this is not a number}").results("");
    }

    @Test
    @DisplayName("Test numericity and fail")
    void intFail() throws Exception {
        TestThat.theInput("{@assert:int /2.3/this is not an int}").throwsBadSyntax("this is not an int.*");
    }

    @Test
    @DisplayName("Test numeric okay")
    void intOk() throws Exception {
        TestThat.theInput("{@assert:int /2/this is not a number}").results("");
    }
}
