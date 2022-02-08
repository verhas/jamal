package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestRangeMacro {

    @Test
    @DisplayName("A range with no specification returns all the lines")
    void testNoRangeNoOp() throws Exception {
        TestThat.theInput("{@range\n" +
                "1\n" +
                "2\n" +
                "3\n" +
                "4\n" +
                "}").results("1\n2\n3\n4\n");
    }

    @Test
    @DisplayName("A range with lines returns one range")
    void testOneRange() throws Exception {
        TestThat.theInput("{@range lines=1..2\n" +
                "1\n" +
                "2\n" +
                "3\n" +
                "4\n" +
                "}").results("1\n2\n");
    }

    @Test
    @DisplayName("A 'ranges' with lines returns the ranges")
    void testTwoRanges() throws Exception {
        TestThat.theInput("{@range lines=1..2;2..3\n" +
                "1\n" +
                "2\n" +
                "3\n" +
                "4\n" +
                "}").results("1\n2\n2\n3\n");
    }

    @Test
    @DisplayName("Range preserves the last new line")
    void testTrailingNoNewLine1() throws Exception {
        TestThat.theInput("{@range lines=1..4\n" +
                "1\n" +
                "2\n" +
                "3\n" +
                "4}").results("1\n2\n3\n4");
    }

    @Test
    @DisplayName("Range preserves the last new line")
    void testTrailingNoNewLine2() throws Exception {
        TestThat.theInput("{@range lines=1..4,4\n" +
                "1\n" +
                "2\n" +
                "3\n" +
                "4}").results("1\n2\n3\n4\n4");
    }
}
