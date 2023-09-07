package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestRepeat {

    @Test
    @DisplayName("Zero times repeat it empty")
    void testEmpty() throws Exception {
        TestThat.theInput("{@repeat (n=0) Abraka dabra}").results("");
    }

    @Test
    @DisplayName("One time repeat is the original string")
    void testOnce() throws Exception {
        TestThat.theInput("{@repeat (n=1)hamala bubbala}").results("hamala bubbala");
    }


    @Test
    @DisplayName("Two times repeat is the string two times")
    void testTwice() throws Exception {
        TestThat.theInput("{#repeat (n=2)s}").results("ss");
    }
}
