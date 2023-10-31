package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
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
    void testReplaceLinesWithRegexMultipleParameters() throws Exception {
        TestThat.theInput(
            "{@replaceLines replace=/^appl(.)/p$1ar/ replace=/^fox/whale/ replace=\"/win(.)e(.) //\" replace=/f(.)rest/$1cean\n" +
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

    @DisplayName("Detect no change when there is no change in the text")
    @Test
    void testNoReplaceError() throws Exception {
        TestThat.theInput(
            "{@replaceLines detectNoChange replace=\"/abra/kadabra\"\n" +
                "zummm" +
                "}").throwsBadSyntax();
    }

    @DisplayName("Detect no change even if one of the changes work")
    @Test
    void testNoReplaceError2() throws Exception {
        TestThat.theInput(
            "{@replaceLines detectNoChange replace=\"/abra/kadabra/zum/zim/\"\n" +
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
