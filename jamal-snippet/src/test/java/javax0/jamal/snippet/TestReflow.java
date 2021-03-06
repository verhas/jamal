package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestReflow {

    @Test
    void testReflow() throws Exception {
        TestThat.theInput(
            "{@define width=11}{@reflow width=10\n" +
                //0123456789
                "this \n" +
                "is\n" +
                "short\n" +
                "Then this is a very long line that will be split into several lines.\n" +
                "\n" +
                "This is a new paragraph.\n" +
                "\n" +
                "\n" +
                "Triple lines are \nconverted to asas\n" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "Some lines just cannot be split." +
                "}"
        ).results("this  is\n" +
            "short Then\n" +
            "this is a\n" +
            "very long\n" +
            "line that will\n" +
            "be split\n" +
            "into several\n" +
            "lines.\n" +
            "\n" +
            "This is a\n" +
            "new\n" +
            "paragraph.\n" +
            "\n" +
            "Triple\n" +
            "lines are \n" +
            "converted to\n" +
            "asas\n" +
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
            "Some lines\n" +
            "just\n" +
            "cannot be split.");
    }

    @Test
    void testReflowUnlimited() throws Exception {
        TestThat.theInput(
            "{@reflow \n" +
                "this \n" +
                "is\n" +
                "short\n" +
                "Then this is a very long line that will be split into several lines.\n" +
                "\n" +
                "This is a new paragraph.\n" +
                "\n" +
                "\n" +
                "Triple lines are \nconverted to asas\n" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n" +
                "Some lines just cannot be split." +
                "}"
        ).results("this  is short Then this is a very long line that will be split into several lines.\n" +
            "\n" +
            "This is a new paragraph.\n" +
            "\n" +
            "Triple lines are  converted to asas aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa Some lines just cannot be split.");
    }


    @Test
    void testReflowMultipleLines() throws Exception {
        TestThat.theInput(
            "{@reflow \n" +
                "Then this is a very long line that will be split into several lines.\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "This is a new paragraph.\n" +
                "}"
        ).results("Then this is a very long line that will be split into several lines.\n" +
            "\n" +
            "This is a new paragraph. ");
    }
}
