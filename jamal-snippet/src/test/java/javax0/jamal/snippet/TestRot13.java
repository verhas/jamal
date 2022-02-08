package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestRot13 {

    @Test
    @DisplayName("Simple rot13 test")
    void testSimpleSample() throws Exception {
        TestThat.theInput("{@rot13 Abraka dabra}").results("Noenxn qnoen");
    }

    @Test
    @DisplayName("Rot13 empty string is an empty string")
    void testEmpty() throws Exception {
        TestThat.theInput("{@rot13}").results("");
    }


    @Test
    @DisplayName("Rot13 twice is the string itself")
    void testRot26() throws Exception {
        TestThat.theInput("{#rot13 {@rot13 abraka dabra}}").results("abraka dabra");
    }
}
