package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestFor {
    @Test
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
}
