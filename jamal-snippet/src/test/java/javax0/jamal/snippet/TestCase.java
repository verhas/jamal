package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestCase {

    @Test
    @DisplayName("case:lower converts properly")
    void testLower() throws Exception {
        TestThat.theInput("{@case:lower this wiLL BE all Lover Case}").results("this will be all lover case");
    }

    @Test
    @DisplayName("case:upper converts properly")
    void testUpper() throws Exception {
        TestThat.theInput("{@case:upper this wiLL BE all UPPer Case}").results("THIS WILL BE ALL UPPER CASE");
    }

    @Test
    @DisplayName("case:cap capitalized the fist character")
    void testCap() throws Exception {
        TestThat.theInput("{@case:cap this wiLL BE all Lover Case}").results("This wiLL BE all Lover Case");
    }

    @Test
    @DisplayName("case:decap decapitalized the fist character")
    void testDeCap() throws Exception {
        TestThat.theInput("{@case:decap This wiLL BE all Lover Case}").results("this wiLL BE all Lover Case");
    }
    @Test
    @DisplayName("case:cap capitalize zero length string")
    void testCapZLS() throws Exception {
        TestThat.theInput("{@case:cap }").results("");
    }

    @Test
    @DisplayName("case:decap decapitalize zero length string")
    void testDeCapZLC() throws Exception {
        TestThat.theInput("{@case:decap}").results("");
    }
}
