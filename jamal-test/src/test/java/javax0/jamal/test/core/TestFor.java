package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestFor {
    @Test
    @DisplayName("Test a simple for loop")
    void testSimpleFor() throws Exception {

        TestThat.theInput(
            "{@for x in (a,b,c,d)= x is either a, b, c or d\n}"

        ).results(
            " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n"
        );
    }

    @Test
    @DisplayName("multi-variable loop")
    void testMultiVariableFor() throws Exception {

        TestThat.theInput(
            "{@for (x,y) in (a|1,b|2,c|3,d|4)= x is y\n}"

        ).results(
            " a is 1\n" +
                " b is 2\n" +
                " c is 3\n" +
                " d is 4\n"
        );
    }

    @Test
    @DisplayName("Test a simple for loop with separator redefinition")
    void testSimpleForForSepRedef() throws Exception {

        TestThat.theInput(
            "{@define $forsep=;}{@for x in (a;b;c;d)= x is either a, b, c or d\n}"

        ).results(
            " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n"
        );
    }

    @Test
    @DisplayName("Test a simple for loop with separator redefinition and trimmed")
    void testSimpleForForSepRedefTrimmed() throws Exception {

        TestThat.theInput(
            "{@for [separator=; trim] x in (a; b; c; d)= x is either a, b, c or d\n}"

        ).results(
            " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n"
        );
    }

    @Test
    @DisplayName("Test a simple for loop with back-ticked separator")
    void testSimpleForWithBacktickedSeparator() throws Exception {

        TestThat.theInput(
            "{@define $forsep=;}{@for x in `)))`a);b);c);d)`)))`= x is either a, b, c or d\n}"

        ).results(
            " a) is either a, b, c or d\n" +
                " b) is either a, b, c or d\n" +
                " c) is either a, b, c or d\n" +
                " d) is either a, b, c or d\n"
        );
    }
}
