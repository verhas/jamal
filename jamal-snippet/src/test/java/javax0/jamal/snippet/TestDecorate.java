package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestDecorate {

    @Test
    void testDecor() throws Exception {
        TestThat.theInput("{@define d(x)=<x>}" +
                        "{@decorate (ratios=\"- 0 1 1 2 0.4\" decorator=d)" +
                        "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}")
                .results("<vac>ation a n <t>h <vac>mztjxn <arbi>tracion ~anrakadabra~ <ia>cion <vu>lture");
    }

    @Test
    void testTwoDecors() throws Exception {
        TestThat.theInput("{@define d1(x)=<x>}{@define d2(x)=/x/}" +
                        "{@decorate (ratios=\"- 0 1 1 2 0.4\" decorator=d1 decorator=d2)" +
                        "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}")
                .results("<vac>/ation/ a n <t>/h/ <vac>/mztjxn/ <arbi>/tracion/ ~anrakadabra~ <ia>/cion/ <vu>/lture/");
    }

    @Test
    void testTwoDecorsRepeated() throws Exception {
        TestThat.theInput("{@define d1(x)=<x>}{@define d2(x)=/x/}" +
                        "{@decorate (repeat ratios=\"- 0 1 1 2 0.4\" decorator=d1 decorator=d2)" +
                        "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}")
                .results("<vac>ation a n <t>h <vac>/mz/tjxn <arbi>/tr/acion ~anrakadabra~ <ia>/c/ion <vu>/l/ture");
    }

    @Test
    void testThreeDecors() throws Exception {
        TestThat.theInput("{@define d1(x)=<x>}{@define d2(x)=/x/}" +
                        "{@decorate (ratios=\"- 0 1 1 2 0.4\" decorator=d1 decorator=d2 decorator=d1)" +
                        "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}")
                .results("<vac>ation a n <t>h <vac>/mz/<t>jxn <arbi>/tr/<ac>ion ~anrakadabra~ <ia>/c/<i>on <vu>/l/ture");
    }

    @Test
    void testDecorNoDeli() throws Exception {
        TestThat.theInput("{@define d(x)=<x>}" +
                "{@decorate (ratios=\"- 0 1 1 2 0.4\" decorator=d delimiters=)" +
                "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}").results("<vac>ation a n <t>h <vac>mztjxn <arbi>tracion ~<anra>kadabra~ <ia>cion <vu>lture");
    }

    @Test
    void testBdNF() throws Exception {
        TestThat.theInput("{@define d(x)=<x>}" +
                "{@decorate (ratios=\"- Y 1 1 2 0.4\" decorator=d)" +
                " vulture}").throwsBadSyntax("Invalid number 'Y' in the parameter '- Y 1 1 2 0\\.4'");
    }

    @Test
    void testBdNF2() throws Exception {
        TestThat.theInput("{@define d(x)=<x>}" +
                "{@decorate (ratios=\"- 0 1 1 2 1.4\" decorator=d)" +
                " vulture}").throwsBadSyntax("Invalid number 1\\.4 at the last position in the parameter \"- 0 1 1 2 1\\.4\"");
    }

    @Test
    void testBdNF3() throws Exception {
        TestThat.theInput("{@decorate (ratios=\"- 0 3 1 2 0.4\")" +
                " vulture}").throwsBadSyntax("Invalid number 3 at the position 2 in the parameter \"- 0 3 1 2 0\\.4\"");
    }

    @Test
    void testBdSF() throws Exception {
        TestThat.theInput("{@decorate (ratios=\"* Y 1 1 2 0.4\")" +
                " vulture}").throwsBadSyntax("The parameter \"ratios\" is malformed, it must look like '- 0 1 1 2 0.4', the first character must be '\\+' or '-'");
    }

    @Test
    @DisplayName("common words bolded")
    void testDecorra() throws Exception {
        TestThat.theInput("{@define d(x)=<x>}" +
                "{@decorate (ratios=\"+ 0 1 1 2 0.4\" decorator=d)" +
                "vacation a n th vacmztjxn arbitracion ~anrakadabra~ iacion vulture}").results("<vac>ation <a> n <t>h <vac>mztjxn <arbi>tracion ~anrakadabra~ <ia>cion <vu>lture");
    }

    @Test
    @DisplayName("returns one character not bolded")
    void testDecorA() throws Exception {
        TestThat.theInput("{@decorate ()a}"
        ).results("a");
    }

    @Test
    @DisplayName("empty input")
    void returnsEmpty() throws Exception {
        TestThat.theInput("{@decorate}").results("");
    }

    @Test
    @DisplayName("using dictionary")
    void usingDictonary() throws Exception {
        TestThat.theInput("{@dictionary\n" +
                "k*ivetel\n" +
                "}" +
                "{@decorate kevetel kivetel}").results("ke vetel k ivetel");
    }

    @Test
    @DisplayName("using dictionary named when there is also 'unnamed' dictionary")
    void usingDictonaryNamed() throws Exception {
        TestThat.theInput("{@dictionary\n" +
                        "k*ivetel\n" +
                        "}" +
                        "{@dictionary id=id\n" +
                        "k*evetel\n" +
                        "*krevetel\n" +
                        "kretek*\n" +
                        "kretes\n" +
                        "}" +
                        "{@decorate (dict=id)kevetel kivetel krevetel kretek kretes}")
                .results("k evetel ki vetel krevetel kretek  kretes ");
    }

    @Test
    @DisplayName("using all three dictionaries named")
    void usingAllDictionariesNamed() throws Exception {
        TestThat.theInput(
                        "{@dictionary id=dict\n" +
                                "k*ivetel\n" +
                                "}" +
                                "{@dictionary id=pf\n" +
                                "abcdefgh\n" +
                                "xyzhubbababubba\n" +
                                "}" +
                                "{@dictionary id=cm\n" +
                                "z\n" +
                                "n\n" +
                                "h\n" +
                                "}" +
                                "{@decorate (dict=dict pf=pf cm=cm ratios=\"+ 0 1 1 2 0.4\")" +
                                "kevetel a b z n t kivetel krevetel kretek kretes aabcdefgh vition}")
                .results(
                        "ke vetel " + // 7 chars, 40% is 2.8char -> 2 chars are bold
                                "a b " + // 1 char, not common, not bold
                                "z  n  " + // 1 char, listed as common, bold
                                "t " + // 1 char, not common, not bold
                                "k ivetel " + // 7 chars, in the dictionary, 1 char is bold
                                "kre vetel " + // 8 chars, 40% is 3.2char -> 3 chars are bold
                                "kr etek kr etes " + // 6 chars, 40% is 2.4char -> 2 chars are bold
                                "a abcdefgh " + // 9 chars, postfix is recognized, postfix is not bold
                                "vi tion"); // postfix is not recognized, 6 chars, 40% is 2.4char -> 2 chars are bold
    }
}
