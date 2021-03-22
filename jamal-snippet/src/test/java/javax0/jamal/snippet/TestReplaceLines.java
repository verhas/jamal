package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestReplaceLines {

    @Test
    void testReplaceLinesWithRegex() throws Exception {
        TestThat.theInput("{@define replace=/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean}\\\n" +
            "{@replaceLines \n" +
            "apple fell off the tree\n" +
            "fox mating in the winter firest\n" +
            "fox mating in the winter forest\n" +
            "}").results("pear fell off the tree\n" +
            "whale mating in the icean\n" +
            "whale mating in the ocean\n"
        );
    }

    @Test
    void testReplaceLinesWithRegexInParams() throws Exception {
        TestThat.theInput(
            "{@replaceLines detectNoChange replace=\"/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean\"\n" +
                "apple fell off the tree\n" +
                "fox mating in the winter firest\n" +
                "fox mating in the winter forest\n" +
                "}").results("pear fell off the tree\n" +
            "whale mating in the icean\n" +
            "whale mating in the ocean\n"
        );
    }

    @Test
    void testReplaceLinesWithRegexInParamsNoNLAtTheEnd() throws Exception {
        TestThat.theInput(
            "{@replaceLines detectNoChange replace=\"/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean\"\n" +
                "apple fell off the tree\n" +
                "fox mating in the winter firest\n" +
                "fox mating in the winter forest}"
        ).results(
            "pear fell off the tree\n" +
                "whale mating in the icean\n" +
                "whale mating in the ocean"
        );
    }

    @Test
    void testNoReplaceError() throws Exception {
        TestThat.theInput(
            "{@replaceLines detectNoChange replace=\"/abra/kadabra\"\n" +
                "zummm" +
                "}").throwsBadSyntax();
    }

    @Test
    void testReplaceLinesWithRegexDeleting() throws Exception {
        TestThat.theInput(
            "{@replaceLines replace=\"/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean/a\"\n" +
                "apple fell off the tree\n" +
                "fox mating in the winter firest\n" +
                "fox mating in the winter forest\n" +
                "}").results("per fell off the tree\n" +
            "whle mting in the icen\n" +
            "whle mting in the ocen\n"
        );
    }

    @Test
    void testReplaceLinesWithReplaceBody() throws Exception {
        TestThat.theInput(
            "{@replaceLines replace=\"\"\n}").throwsBadSyntax();
    }

    @Test
    void testReplaceLinesWithBadRegex() throws Exception {
        TestThat.theInput("{@replaceLines replace=\"/(/\"\n}").throwsBadSyntax();
    }
}
