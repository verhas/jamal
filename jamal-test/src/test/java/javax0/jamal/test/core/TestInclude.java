package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestInclude {

    @Test
    @DisplayName("You can include only certain lines from a file")
    void testPartialInclude() throws Exception {
        TestThat.theInput("{@include [lines=1..2] res:import/lineNumbers.txt}").ignoreLineEnding().results("01\n02\n");
    }

    @Test
    @DisplayName("Multiple ranges can be specified")
    void testMultipleRanges() throws Exception {
        TestThat.theInput("{@include [lines=1..2,10..14,16..18] res:import/lineNumbers.txt}").ignoreLineEnding().results("01\n02\n10\n11\n12\n13\n14\n16\n17\n18\n");
    }

    @Test
    @DisplayName("You can specify a range using negative numbers")
    void testNegativeRange() throws Exception {
        TestThat.theInput("{@include [lines=-1..-2] res:import/lineNumbers.txt}").ignoreLineEnding().results("19\n18\n");
    }

    @Test
    @DisplayName("A one-line range can be specified with a single number")
    void testOneLineRange() throws Exception {
        TestThat.theInput("{@include [lines=1] res:import/lineNumbers.txt}").ignoreLineEnding().results("01\n");
    }

    @Test
    @DisplayName("A one-line range can be specified with a single number along with multiple ranges")
    void testOneLineRangeWithMultipleRanges() throws Exception {
        TestThat.theInput("{@include [lines=1..2,7,-2..-1] res:import/lineNumbers.txt}").ignoreLineEnding().results("01\n02\n07\n18\n19\n");
    }

    @Test
    @DisplayName("A range can also descent")
    void testDescendingRange() throws Exception {
        TestThat.theInput("{@include [lines=10..1] res:import/lineNumbers.txt}").ignoreLineEnding().results("10\n09\n08\n07\n06\n05\n04\n03\n02\n01\n");
    }

    @Test
    @DisplayName("Ranges can be specified with spaces arbitrary in the middle")
    void testRangeWithSpaces() throws Exception {
        TestThat.theInput("{@include [lines=\"1.. 2,  3.. 4,  5.. 7\"] res:import/lineNumbers.txt}").ignoreLineEnding().results("01\n02\n03\n04\n05\n06\n07\n");
    }

    @Test
    @DisplayName("Range cannot contain zero as a boundary value")
    void testRangeWithZero() throws Exception {
        TestThat.theInput("{@include [lines=0..1] res:import/lineNumbers.txt}").throwsBadSyntax("The line range 0\\.\\.1 is not valid");
    }

    @Test
    @DisplayName("Range boundary too large just gets capped")
    void testRangeWithTooLargeBoundary() throws Exception {
        TestThat.theInput("{@include [lines=18..100] res:import/lineNumbers.txt}").ignoreLineEnding().results("18\n19\n");
    }

    @Test
    @DisplayName("Range boundary too small just gets capped")
    void testRangeWithTooSmallBoundary() throws Exception {
        TestThat.theInput("{@include [lines=-100..+1] res:import/lineNumbers.txt}").ignoreLineEnding().results("01\n");
    }

}
