package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestReplaceLines {

    @Test
    void testReplaceLinesWithRegex() throws Exception {
        TestThat.theInput("{@define replace=/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean}\\\n" +
            "{@options regex}" +
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
            "{@options regex}" +
            "{@replaceLines replace=\"/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean\"\n" +
            "apple fell off the tree\n" +
            "fox mating in the winter firest\n" +
            "fox mating in the winter forest\n" +
            "}").results("pear fell off the tree\n" +
            "whale mating in the icean\n" +
            "whale mating in the ocean\n"
        );
    }
}
