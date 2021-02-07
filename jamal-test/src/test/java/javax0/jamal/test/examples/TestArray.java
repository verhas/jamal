package javax0.jamal.test.examples;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestArray {

    @ParameterizedTest
    @CsvSource(value = {
        // snippet TestArray_test1
        "{@array /1/x/aaa/z},aaa",
        // end snippet
        "{@array `\\)\\(|\\(|\\)`0)(aaa)(a)(z)},aaa",
        "{@array /2/x/aaa/aaa},aaa",
    })
    void testValidArrayAccesses(final String source, final String result) throws Exception {
        TestThat.theInput(source).results(result);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "{@array /3/x/aaa/z}",
        "{@array /-1/x/aaa/z}",
        "{@array 1 aaa}",
        "{@array /habakukk/aaa/a/z}",
    })
    void testInvalidArrayAccesses(final String source) throws Exception {
        TestThat.theInput(source).throwsBadSyntax();
    }
}
