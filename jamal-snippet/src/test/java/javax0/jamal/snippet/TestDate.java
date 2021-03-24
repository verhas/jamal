package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestDate {
    @Test
    void testDate() throws Exception {
        TestThat.theInput("{@date yyyy}").results("2021");
    }
}
