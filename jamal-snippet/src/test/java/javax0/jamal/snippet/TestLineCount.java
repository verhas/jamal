package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestLineCount {

    @Test
    void testEmptyLineCount() throws Exception {
        TestThat.theInput("{@lineCount}").results("0");
    }

    @Test
    void testOneLineCount() throws Exception {
        TestThat.theInput("{@lineCount\n" +
            "}").results("1");
    }    @Test
    void testGeneralLineCount() throws Exception {
        TestThat.theInput("{@lineCount\n" +
            "does not realy matter\n" +
            "what is written here, this is just\n" +
            "three lines}").results("3");
    }

}
