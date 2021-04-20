package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestDefine {

    /**
     * When the user defined macro name itself is defined as the result of a macro evaluation it is forbidden to get the
     * separator character as the result of the evaluation. In this case `z` is evaluated to `a|` and that would make
     * `|` to the separator character for the arguments (we have here only one). It could be
     *
     * @throws Exception when the test fails
     */
    @Test
    void testMacroNameFromMacro() throws Exception {
        TestThat.theInput(
            "{@define z=a|}\n" +
                "{@define a(k)=hkh}\n" +
                "{@try! {{z}K}}\n" +
                "\n" +
                "{@define z=a}\n" +
                "{@define a(k)=hkh}\n" +
                "{{z}|K}\n" +
                "\n" +
                "this would be correct"

        ).results(

            "\n" +
                "\n" +
                "Macro evaluated result user defined macro name contains the separator. Must not.\n" +
                "\n" +
                "\n" +
                "\n" +
                "hKh\n" +
                "\n" +
                "this would be correct"
        );
    }

    @Test
    void testAlphaSeparator() throws Exception {
        TestThat.theInput("{@try! {@define a(b,c)=bika cica}{a HkukkHmukk}}").results("Invalid separator character 'H' ");
    }

    @Test
    void testEvaluationOrder() throws Exception {
        TestThat.theInput(
            "{@define firstName=Julia}{@define k(h)=h, {firstName} h{@define son=Junior Bond}}" +
                "{k /Bond{@define firstName=James}}\n" +
                "{k /Bond}\n" +
                "{son}"

        ).results(

            "Bond, James Bond\n" +
                "Bond, Julia Bond\n" +
                "Junior Bond"
        );
    }

    @Test
    void testVerbatimAlsoClosesTheScope() throws Exception {
        TestThat.theInput(
            "{@define a=this is it}{@define b={a}}{#define c={@verbatim b}}{c} {@verbatim c}"
        ).results(
            "this is it {a}"
        );
    }

    @Test
    void testLenient() throws Exception {
        TestThat.theInput(
            "{@options lenient}{@define z()=zzz}{z aaa}"
        ).results(
            "zzz"
        );
    }

    @Test
    void testFish() throws Exception {
        TestThat.theInput(
            "{@define z(*a,*b,*c,*d)=When a *a can *b then *c can *d}\n" +
                "{z /leopard and a *c/run/fish/fly}"
        ).results(
            "\n" +
                "When a leopard and a *c can run then fish can fly"
        );
    }

    @Test
    void testFor() throws Exception {
        TestThat.theInput(
            "{@for x in (a,b,c,d)= x is either a, b, c or d\n" +
                "}{@define $forsep=;}{@for x in (a;b;c;d)= x is either a, b, c or d\n" +
                "}"
        ).results(
            " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n" +
                " a is either a, b, c or d\n" +
                " b is either a, b, c or d\n" +
                " c is either a, b, c or d\n" +
                " d is either a, b, c or d\n"
        );
    }

    @Test
    void testNoRedefine() throws Exception {
        TestThat.theInput(
            "{@define a=1}{@define ! a=2}"
        ).throwsBadSyntax("The macro 'a' was already defined.");
        TestThat.theInput(
            "{@define a=1}{#block {@define ! a=2}}"
        ).throwsBadSyntax("The macro 'a' was already defined.");
    }

    @Test
    void testNameEvaluationError() throws Exception {
        TestThat.theInput(
            "{@try! {@define z=a}{@define su=/}\n" +
                "{@define a(k)=hkh}\n" +
                "{{z}{su}K}}"
        ).results(
            "Macro evaluated result user defined macro name contains the separator. Must not."
        );
    }

    @Test
    void testRecursiveDefinitions() throws Exception {
        TestThat.theInput(
            "{@define wilfred=define}{#{wilfred} alfred=wilfred}{alfred}\n" +
                "{@define black=white}{@define white=black}{{black}} {{{black}}}\n" +
                "{@define bla=whi}{@define ck=te}{{bla}\\\n" +
                "{ck}} {{{bla}{ck}}}"
        ).results(
            "wilfred\n" +
                "black white\n" +
                "black white"
        );
    }

    @Test
    void testOneArgumentMacro() throws Exception {
        TestThat.theInput(
            "{@comment When the user define macro has only one argument then the separator character may be missing.}\n" +
                "{@define enclose(a)=<||a||>}{enclose this text}\n" +
                "{enclose|this text}\n" +
                "{enclose |this text}\n" +
                "{enclose |+this text}\n" +
                "{enclose ||this text}\n" +
                "{enclose | this text}\n" +
                "{enclose | this||text}"
        ).results(
            "\n" +
                "<||this text||>\n" +
                "<||this text||>\n" +
                "<||this text||>\n" +
                "<||+this text||>\n" +
                "<|||this text||>\n" +
                "<|| this text||>\n" +
                "<|| this||text||>"
        );
    }

    @Test
    void testOneArgMacroNesting() throws Exception {
        TestThat.theInput(
            "{@define a(x)=<<|x|>>}{@define b=55}{a {b}}"
        ).results(
            "<<|55|>>"
        );
    }

    @Test
    @DisplayName("Test globally defined macro optional redefinition")
    void testGlobalOptionalRedefine() throws Exception {
        TestThat.theInput("{@define :a=1}{#ident {@define ?a=2}{a}}").results("1");
        TestThat.theInput("{@define :a=1}{#ident {@define ?:a=2}{a}}").results("1");
        TestThat.theInput("{@define :a=1}{#ident {@define ?:a=2}{:a}}").results("1");
    }

    @Test
    @DisplayName("Test that macro can be defined to behave verbatim")
    void testVerbatimDefine() throws Exception {
        TestThat.theInput("{@define~ a={@code}}{a}").results("{@code}");
        TestThat.theInput("{@define ~ a={@code}}{a}").results("{@code}");
        TestThat.theInput("{@define ?~ a={@code}}{a}").results("{@code}");
        TestThat.theInput("{@define !~ a={@code}}{a}").results("{@code}");
        TestThat.theInput("{@define ~! a={@code}}{a}").results("{@code}");
        TestThat.theInput("{@define ~? a={@code}}{a}").results("{@code}");
        TestThat.theInput("{@define ~?~ a={@code}}{a}").throwsBadSyntax("define.*has no.*");

        TestThat.theInput("{@define b=2}{@define ~ a={b}}{!a}").results("2");
    }
}
