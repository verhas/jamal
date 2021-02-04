package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestComment {

    @Test
    void testComment()throws Exception{
        TestThat.theInput(
            "{@comment does produce empty string}"
        ).results(
            ""
        );
    }
}
