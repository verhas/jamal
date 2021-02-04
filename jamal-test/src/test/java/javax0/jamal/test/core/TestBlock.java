package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestBlock {
    @Test
    void testThatBlockIsLikeComment()throws Exception{
        TestThat.theInput(
            "{#block does produce empty string}"
        ).results(
            ""
        );
    }
}
