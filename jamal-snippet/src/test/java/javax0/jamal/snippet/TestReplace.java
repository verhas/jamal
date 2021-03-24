package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestReplace {

    @Test
    void testSimpleReplace() throws Exception {
        TestThat.theInput("{@replace |alma korte barack|alma|apple}").results("apple korte barack");
    }

    @Test
    void testMultipleReplace() throws Exception {
        TestThat.theInput("{@replace `\\||=`alma korte barack|alma=apple|korte=pear|barack=peach}").results("apple pear peach");
    }

    @Test
    void testSimpleReplaceRegex() throws Exception {
        TestThat.theInput("{@options regex}{@replace |alma korte barack|a.ma|apple}").results("apple korte barack");
    }

    @Test
    void testSimpleReplaceRegexFromParameter() throws Exception {
        TestThat
            .theInput("{@options ~regex}{@replace (regex) |alma korte barack|a.ma|apple}")
            .results("apple korte barack");
    }

    @Test
    void testMultipleReplaceRegex() throws Exception {
        TestThat.theInput("{@options regex}{@replace `\\||=`alma korte barack|a..a=apple|k.rte=pear|b.rack=peach}").results("apple pear peach");
    }

    @Test
    void testDelete1() throws Exception {
        TestThat.theInput("{@replace |alma korte barack|alma|}").results(" korte barack");
    }

    @Test
    void testDelete2() throws Exception {
        TestThat.theInput("{@replace |alma korte barack|alma}").results(" korte barack");
    }

    @Test
    void testDeleteRegex() throws Exception {
        TestThat.theInput("{#replace |{@options regex}alma     korte     barack|\\s+| }").results("alma korte barack");
    }

    @Test
    void testNotEnoughArgument() throws Exception {
        TestThat.theInput("{#replace |alma     korte     barack}").throwsBadSyntax();
    }
    @Test
    void testBadRegex() throws Exception {
        TestThat.theInput("{#replace (regex) |alma     korte     barack|(\\s+}").throwsBadSyntax();
    }

}
