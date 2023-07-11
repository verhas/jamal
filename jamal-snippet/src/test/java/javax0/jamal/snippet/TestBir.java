package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestBir {

    @Test
    void testBir() throws Exception {
        TestThat.theInput("{@bir (ratios=\"- 0 1 1 2 0.4\" prefix=< postfix=>)" +
                "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}").results("<vac>ation a n <t>h <vac>mztjxn <arbi>tracion ~anrakadabra~ <ia>cion <vu>lture");
    }

    @Test
    void testBirNoDeli() throws Exception {
        TestThat.theInput("{@bir (ratios=\"- 0 1 1 2 0.4\" prefix=< postfix=> delimiters=)" +
                "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}").results("<vac>ation a n <t>h <vac>mztjxn <arbi>tracion ~<anra>kadabra~ <ia>cion <vu>lture");
    }

    @Test
    void testBdNF() throws Exception {
        TestThat.theInput("{@bir (ratios=\"- Y 1 1 2 0.4\" prefix=< postfix=>)" +
                " vulture}").throwsBadSyntax("Invalid number 'Y' in the parameter '- Y 1 1 2 0\\.4'");
    }    @Test
    void testBdNF2() throws Exception {
        TestThat.theInput("{@bir (ratios=\"- 0 1 1 2 1.4\" prefix=< postfix=>)" +
                " vulture}").throwsBadSyntax("Invalid number 1\\.4 at the position 5 in the parameter \"- 0 1 1 2 1\\.4\"");
    }    @Test
    void testBdNF3() throws Exception {
        TestThat.theInput("{@bir (ratios=\"- 0 3 1 2 0.4\" prefix=< postfix=>)" +
                " vulture}").throwsBadSyntax("Invalid number 3 at the position 2 in the parameter \"- 0 3 1 2 0\\.4\"");
    }

    @Test
    void testBdSF() throws Exception {
        TestThat.theInput("{@bir (ratios=\"* Y 1 1 2 0.4\" prefix=< postfix=>)" +
                " vulture}").throwsBadSyntax("The parameter \"ratios\" is malformed, it must look like '- 0 1 1 2 0.4', the first character must be '\\+' or '-'");
    }

    @Test
    void testBirra() throws Exception {
        TestThat.theInput("{@bir (ratios=\"+ 0 1 1 2 0.4\" prefix=< postfix=>)" +
                "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}").results("<vac>ation <a> n <t>h <vac>mztjxn <arbi>tracion ~anrakadabra~ <ia>cion <vu>lture");
    }

    @Test
    void testBirA() throws Exception {
        TestThat.theInput("{@bir ()a}"
        ).results("a");
    }

    @Test
    void returnsEmpty() throws Exception {
        TestThat.theInput("{@bir}").results("");
    }

}
