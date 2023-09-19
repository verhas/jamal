package javax0.jamal.test.parser;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Parser;
import javax0.jamal.engine.Processor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestParser {

    private static void assertNodeText(final Parser.ASTNode node, final String input) {
        final String fmt = "%s[%s,%s]'%s'";
        Assertions.assertEquals(
                String.format(fmt, node.type, node.start, node.end, node.text),
                String.format(fmt, node.type, node.start, node.end, input.substring(node.start, node.end)));
        for (final var child : node.children) {
            assertNodeText(child, input);
        }
    }

    private static void test(final String input, final String expected) throws BadSyntax {
        final var processor = new Processor();
        final var node = Parser.parse(processor, input);
        Assertions.assertEquals(expected, node+node.toStringList());
        assertNodeText(node, input);
    }

    @Test
    @DisplayName("Test with a simple, not nested input, using ud and bi macros")
    void simpleTest() throws Exception {
        test("some text {macro} some more text{@define aaa} jupp",
                "LIST[0,50] 'some text {macro} some more text{@define aaa} jupp'\n" +
                        "  TEXT[0,10] 'some text '\n" +
                        "  OPEN[10,11] '{'\n" +
                        "  ID[11,16] 'macro'\n" +
                        "  CLOSE[16,17] '}'\n" +
                        "  TEXT[17,32] ' some more text'\n" +
                        "  OPEN[32,33] '{'\n" +
                        "  BIMCHAR[33,34] '@'\n" +
                        "  ID[34,40] 'define'\n" +
                        "  LIST[40,44] ' aaa'\n" +
                        "    TEXT[40,44] ' aaa'\n" +
                        "  CLOSE[44,45] '}'\n" +
                        "  TEXT[45,50] ' jupp'\n" +
                        "LIST[0,50] 'some text {macro} some more text{@define aaa} jupp'\n" +
                        "TEXT[0,10] 'some text '\n" +
                        "OPEN[10,11] '{'\n" +
                        "ID[11,16] 'macro'\n" +
                        "CLOSE[16,17] '}'\n" +
                        "TEXT[17,32] ' some more text'\n" +
                        "OPEN[32,33] '{'\n" +
                        "BIMCHAR[33,34] '@'\n" +
                        "ID[34,40] 'define'\n" +
                        "TEXT[40,44] ' aaa'\n" +
                        "CLOSE[44,45] '}'\n" +
                        "TEXT[45,50] ' jupp'\n");
    }

    @Test
    @DisplayName("Test with undefined bi macro")
    void testUndefBiMacro() throws Exception {
        test("aa{@wuppalashanakitorkak lippiti makkiti hombres}jupp",
                "LIST[0,53] 'aa{@wuppalashanakitorkak lippiti makkiti hombres}jupp'\n" +
                        "  TEXT[0,2] 'aa'\n" +
                        "  OPEN[2,3] '{'\n" +
                        "  BIMCHAR[3,4] '@'\n" +
                        "  ID[4,24] 'wuppalashanakitorkak'\n" +
                        "  TEXT[24,48] ' lippiti makkiti hombres'\n" +
                        "  CLOSE[48,49] '}'\n" +
                        "  TEXT[49,53] 'jupp'\n" +
                        "LIST[0,53] 'aa{@wuppalashanakitorkak lippiti makkiti hombres}jupp'\n" +
                        "TEXT[0,2] 'aa'\n" +
                        "OPEN[2,3] '{'\n" +
                        "BIMCHAR[3,4] '@'\n" +
                        "ID[4,24] 'wuppalashanakitorkak'\n" +
                        "TEXT[24,48] ' lippiti makkiti hombres'\n" +
                        "CLOSE[48,49] '}'\n" +
                        "TEXT[49,53] 'jupp'\n");
    }

    @Test
    @DisplayName("Test with escape that contains macro opening")
    void testEscape() throws Exception {
        test("aa{@escape `aa` { `aa`}jupp",
                "LIST[0,27] 'aa{@escape `aa` { `aa`}jupp'\n" +
                        "  TEXT[0,2] 'aa'\n" +
                        "  OPEN[2,3] '{'\n" +
                        "  BIMCHAR[3,4] '@'\n" +
                        "  ID[4,10] 'escape'\n" +
                        "  TEXT[10,22] ' `aa` { `aa`'\n" +
                        "  CLOSE[22,23] '}'\n" +
                        "  TEXT[23,27] 'jupp'\n" +
                        "LIST[0,27] 'aa{@escape `aa` { `aa`}jupp'\n" +
                        "TEXT[0,2] 'aa'\n" +
                        "OPEN[2,3] '{'\n" +
                        "BIMCHAR[3,4] '@'\n" +
                        "ID[4,10] 'escape'\n" +
                        "TEXT[10,22] ' `aa` { `aa`'\n" +
                        "CLOSE[22,23] '}'\n" +
                        "TEXT[23,27] 'jupp'\n");
    }

    @Test
    @DisplayName("Test with macro calling sep")
    void testWithSep() throws Exception {
        test("aa {@sep [ ]} [alma] birma",
                "LIST[0,26] 'aa {@sep [ ]} [alma] birma'\n" +
                        "  TEXT[0,3] 'aa '\n" +
                        "  OPEN[3,4] '{'\n" +
                        "  BIMCHAR[4,5] '@'\n" +
                        "  ID[5,8] 'sep'\n" +
                        "  LIST[8,12] ' [ ]'\n" +
                        "    TEXT[8,12] ' [ ]'\n" +
                        "  TEXT[13,14] ' '\n" +
                        "  OPEN[14,15] '['\n" +
                        "  ID[15,19] 'alma'\n" +
                        "  CLOSE[19,20] ']'\n" +
                        "  TEXT[20,26] ' birma'\n" +
                        "LIST[0,26] 'aa {@sep [ ]} [alma] birma'\n" +
                        "TEXT[0,3] 'aa '\n" +
                        "OPEN[3,4] '{'\n" +
                        "BIMCHAR[4,5] '@'\n" +
                        "ID[5,8] 'sep'\n" +
                        "TEXT[8,12] ' [ ]'\n" +
                        "TEXT[13,14] ' '\n" +
                        "OPEN[14,15] '['\n" +
                        "ID[15,19] 'alma'\n" +
                        "CLOSE[19,20] ']'\n" +
                        "TEXT[20,26] ' birma'\n");
    }

    @Test
    @DisplayName("Test with macro calling sep and then sep reset")
    void testWithSepSep() throws Exception {
        test("aa {@sep [ ]} [alma] [#sep] {birma}",
                "LIST[0,35] 'aa {@sep [ ]} [alma] [#sep] {birma}'\n" +
                        "  TEXT[0,3] 'aa '\n" +
                        "  OPEN[3,4] '{'\n" +
                        "  BIMCHAR[4,5] '@'\n" +
                        "  ID[5,8] 'sep'\n" +
                        "  LIST[8,12] ' [ ]'\n" +
                        "    TEXT[8,12] ' [ ]'\n" +
                        "  TEXT[13,14] ' '\n" +
                        "  OPEN[14,15] '['\n" +
                        "  ID[15,19] 'alma'\n" +
                        "  CLOSE[19,20] ']'\n" +
                        "  TEXT[20,21] ' '\n" +
                        "  OPEN[21,22] '['\n" +
                        "  BIMCHAR[22,23] '#'\n" +
                        "  ID[23,26] 'sep'\n" +
                        "  TEXT[27,28] ' '\n" +
                        "  OPEN[28,29] '{'\n" +
                        "  ID[29,34] 'birma'\n" +
                        "  CLOSE[34,35] '}'\n" +
                        "LIST[0,35] 'aa {@sep [ ]} [alma] [#sep] {birma}'\n" +
                        "TEXT[0,3] 'aa '\n" +
                        "OPEN[3,4] '{'\n" +
                        "BIMCHAR[4,5] '@'\n" +
                        "ID[5,8] 'sep'\n" +
                        "TEXT[8,12] ' [ ]'\n" +
                        "TEXT[13,14] ' '\n" +
                        "OPEN[14,15] '['\n" +
                        "ID[15,19] 'alma'\n" +
                        "CLOSE[19,20] ']'\n" +
                        "TEXT[20,21] ' '\n" +
                        "OPEN[21,22] '['\n" +
                        "BIMCHAR[22,23] '#'\n" +
                        "ID[23,26] 'sep'\n" +
                        "TEXT[27,28] ' '\n" +
                        "OPEN[28,29] '{'\n" +
                        "ID[29,34] 'birma'\n" +
                        "CLOSE[34,35] '}'\n");
    }

    @Test
    @DisplayName("Test with macro calling sep inside local scope")
    void testWithSepLocal() throws Exception {
        test("aa { here {@sep [ ]} [alma]} [#sep] {birma}",
                "LIST[0,43] 'aa { here {@sep [ ]} [alma]} [#sep] {birma}'\n" +
                        "  TEXT[0,3] 'aa '\n" +
                        "  OPEN[3,4] '{'\n" +
                        "  TEXT[4,5] ' '\n" +
                        "  ID[5,9] 'here'\n" +
                        "  LIST[9,27] ' {@sep [ ]} [alma]'\n" +
                        "    TEXT[9,10] ' '\n" +
                        "    OPEN[10,11] '{'\n" +
                        "    BIMCHAR[11,12] '@'\n" +
                        "    ID[12,15] 'sep'\n" +
                        "    LIST[15,19] ' [ ]'\n" +
                        "      TEXT[15,19] ' [ ]'\n" +
                        "    TEXT[20,21] ' '\n" +
                        "    OPEN[21,22] '['\n" +
                        "    ID[22,26] 'alma'\n" +
                        "    CLOSE[26,27] ']'\n" +
                        "  TEXT[28,36] ' [#sep] '\n" +
                        "  OPEN[36,37] '{'\n" +
                        "  ID[37,42] 'birma'\n" +
                        "  CLOSE[42,43] '}'\n" +
                        "LIST[0,43] 'aa { here {@sep [ ]} [alma]} [#sep] {birma}'\n" +
                        "TEXT[0,3] 'aa '\n" +
                        "OPEN[3,4] '{'\n" +
                        "TEXT[4,5] ' '\n" +
                        "ID[5,9] 'here'\n" +
                        "TEXT[9,10] ' '\n" +
                        "OPEN[10,11] '{'\n" +
                        "BIMCHAR[11,12] '@'\n" +
                        "ID[12,15] 'sep'\n" +
                        "TEXT[15,19] ' [ ]'\n" +
                        "TEXT[20,21] ' '\n" +
                        "OPEN[21,22] '['\n" +
                        "ID[22,26] 'alma'\n" +
                        "CLOSE[26,27] ']'\n" +
                        "TEXT[28,36] ' [#sep] '\n" +
                        "OPEN[36,37] '{'\n" +
                        "ID[37,42] 'birma'\n" +
                        "CLOSE[42,43] '}'\n");
    }

    @Test
    @DisplayName("Test with macro deeply nested")
    void testDeeplyNested() throws Exception {
        test("{@define zima(a)={@define k={@ident jajja}} {zupp}}",
                "LIST[0,51] '{@define zima(a)={@define k={@ident jajja}} {zupp}}'\n" +
                        "  OPEN[0,1] '{'\n" +
                        "  BIMCHAR[1,2] '@'\n" +
                        "  ID[2,8] 'define'\n" +
                        "  LIST[8,50] ' zima(a)={@define k={@ident jajja}} {zupp}'\n" +
                        "    TEXT[8,17] ' zima(a)='\n" +
                        "    OPEN[17,18] '{'\n" +
                        "    BIMCHAR[18,19] '@'\n" +
                        "    ID[19,25] 'define'\n" +
                        "    LIST[25,42] ' k={@ident jajja}'\n" +
                        "      TEXT[25,28] ' k='\n" +
                        "      OPEN[28,29] '{'\n" +
                        "      BIMCHAR[29,30] '@'\n" +
                        "      ID[30,35] 'ident'\n" +
                        "      LIST[35,41] ' jajja'\n" +
                        "        TEXT[35,41] ' jajja'\n" +
                        "      CLOSE[41,42] '}'\n" +
                        "    CLOSE[42,43] '}'\n" +
                        "    TEXT[43,44] ' '\n" +
                        "    OPEN[44,45] '{'\n" +
                        "    ID[45,49] 'zupp'\n" +
                        "    CLOSE[49,50] '}'\n" +
                        "  CLOSE[50,51] '}'\n" +
                        "LIST[0,51] '{@define zima(a)={@define k={@ident jajja}} {zupp}}'\n" +
                        "OPEN[0,1] '{'\n" +
                        "BIMCHAR[1,2] '@'\n" +
                        "ID[2,8] 'define'\n" +
                        "TEXT[8,17] ' zima(a)='\n" +
                        "OPEN[17,18] '{'\n" +
                        "BIMCHAR[18,19] '@'\n" +
                        "ID[19,25] 'define'\n" +
                        "TEXT[25,28] ' k='\n" +
                        "OPEN[28,29] '{'\n" +
                        "BIMCHAR[29,30] '@'\n" +
                        "ID[30,35] 'ident'\n" +
                        "TEXT[35,41] ' jajja'\n" +
                        "CLOSE[41,42] '}'\n" +
                        "CLOSE[42,43] '}'\n" +
                        "TEXT[43,44] ' '\n" +
                        "OPEN[44,45] '{'\n" +
                        "ID[45,49] 'zupp'\n" +
                        "CLOSE[49,50] '}'\n" +
                        "CLOSE[50,51] '}'\n");
    }

}
