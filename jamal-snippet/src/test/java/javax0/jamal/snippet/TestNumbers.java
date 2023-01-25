package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestNumbers {

    @Test
    @DisplayName("Run from zero to ten")
    void testZeroToTen() throws Exception {
        TestThat.theInput("{@numbers to=10}").results("0,1,2,3,4,5,6,7,8,9");
    }

    @Test
    @DisplayName("Run from one to ten")
    void testOneToTen() throws Exception {
        TestThat.theInput("{@numbers from=1 \n\nto=10}").results("1,2,3,4,5,6,7,8,9");
    }

    @Test
    @DisplayName("Run from one to ten step 2")
    void testOneToTenByTwo() throws Exception {
        TestThat.theInput("{@numbers from=1 to=10 by=2}").results("1,3,5,7,9");
    }
    @Test
    @DisplayName("Step=0 throws up")
    void testStepZero() throws Exception {
        TestThat.theInput("{@numbers from=1 to=10 by=0}").throwsBadSyntax();
    }
    @Test
    @DisplayName("Test negative step value")
    void testNegativeStepValue() throws Exception {
        TestThat.theInput("{@numbers from=10 to=1 by=-2}").results("10,8,6,4,2");
    }
}
