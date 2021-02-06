package javax0.jamal.test.examples;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;
// snippet TestSpacer
public class TestSpacer {

    @Test
    void spacerSpacesInput() throws Exception {
        TestThat.theInput(
            "{@spacer This is an interesting\n" +
                "two line sentence}"
        ).results("T h i s   i s   a n   i n t e r e s t i n g \n" +
            "t w o   l i n e   s e n t e n c e");
    }
}
// end snippet