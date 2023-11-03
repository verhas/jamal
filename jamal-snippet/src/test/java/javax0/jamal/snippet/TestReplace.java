package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestReplace {

    @DisplayName("replace one word")
    @Test
    void testSimpleReplace() throws Exception {
        TestThat.theInput("{@replace |alma korte barack|alma|apple}").results("apple korte barack");
    }

    @DisplayName("replace different strings")
    @Test
    void testMultipleReplace() throws Exception {
        TestThat.theInput("{@replace `\\||=`alma korte barack|alma=apple|korte=pear|barack=peach}").results("apple pear peach");
    }

    @DisplayName("replace one word using regular expression, regex is option")
    @Test
    void testSimpleReplaceRegex() throws Exception {
        TestThat.theInput("{@options regex}{@replace |alma korte barack|a.ma|apple}")
                .results("apple korte barack");
    }

    @DisplayName("replace one word using regular expression, regex is parop")
    @Test
    void testSimpleReplaceRegexFromParameter() throws Exception {
        TestThat
                .theInput("{@options ~regex}{@replace (regex) `(/|->)`alma korte barack/a.ma->apple}")
                .results("apple korte barack");
    }

    @DisplayName("replace different strings with different regular expressions")
    @Test
    void testMultipleReplaceRegex() throws Exception {
        TestThat.theInput("{@options regex}{@replace `(/|->)`alma korte barack/a..a->apple/k.rte->pear/b.rack->peach}")
                .results("apple pear peach");
    }

    @DisplayName("user replace to delete a string (replace with specified empty string)")
    @Test
    void testDelete1() throws Exception {
        TestThat.theInput("{@replace |alma korte barack|alma|}").results(" korte barack");
    }

    @DisplayName("user replace to delete a string (replace with implicit empty string)")
    @Test
    void testDelete2() throws Exception {
        TestThat.theInput("{@replace |alma korte barack|alma}").results(" korte barack");
    }

    @DisplayName("Use regex to delete multiple spaces to one space")
    @Test
    void testDeleteRegex() throws Exception {
        TestThat.theInput("{#replace |{@options regex}alma     korte     barack|\\s+| }").results("alma korte barack");
    }

    @DisplayName("Use the global macro $REGEX to define the separator between the replace parts")
    @Test
    void testRegexDefinedByMacro() throws Exception {
        TestThat.theInput("{@define $REGEX()=/|->}{#replace (regex)alma     korte     barack/\\s+ -> }").results("alma korte barack");
    }

    @DisplayName("Detect when there are not enough parts of the macro")
    @Test
    void testNotEnoughArgument() throws Exception {
        TestThat.theInput("{#replace |alma     korte     barack}").throwsBadSyntax();
    }

    @DisplayName("Detect when there is an error in the regular expression")
    @Test
    void testBadRegex() throws Exception {
        TestThat.theInput("{#replace (regex) |alma     korte     barack|(\\s+}").throwsBadSyntax();
    }

    @DisplayName("Detect no change when one of the search texts is not found")
    @Test
    void testStringNotFound() throws Exception {
        TestThat.theInput("{#replace (detectNoChange)|alma korte barack|alma|apple|korte|pear|peach|barack}")
                .throwsBadSyntax();
    }

    @DisplayName("Detect no change when one of the search regular expressions is not found")
    @Test
    void testRegExNotFound() throws Exception {
        TestThat.theInput("{#replace (regex detectNoChange)|alma korte barack|alma|apple|korte|pear|peach|barack}")
                .throwsBadSyntax();
    }
}
