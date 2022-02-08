package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TestSnipTransform {

    @Test
    @DisplayName("snip:transform - simple kill and trim, number auto added")
    void testCompoundSnipTransfer() throws Exception {
        TestThat.theInput(""
                + "{@snip:transform actions=kill,trim,number margin=3 start=10 step=10 \n"
                + "       \n" // empty line will be killed by default
                + "       papirus mapirus\n"
                + "         tabbed in hamarin\n"
                + "       \n" // empty line will be killed by default
                + "       tabbed in hamarine\n"
                + "}"
        ).results(""
                + "10.    papirus mapirus\n"
                + "20.      tabbed in hamarin\n"
                + "30.    tabbed in hamarine\n");
    }

    @Test
    @DisplayName("snip:transform - throw using parameter without action")
    void testThrowsWhenActionIsNotDefined() throws Exception {
        TestThat.theInput(""
                + "{@snip:transform actions=kill,trim margin=3 start=10 step=10 \n"
                + "       \n" // empty line will be killed by default
                + "       papirus mapirus\n"
                + "         tabbed in hamarin\n"
                + "       \n" // empty line will be killed by default
                + "       tabbed in hamarine\n"
                + "}"
        ).throwsBadSyntax("'start' can be used only when 'number' specified as action or parameter.");
    }

    @Test
    @DisplayName("snip:transform - simple number and trim, kill auto added")
    void testKillCanBeUsedAsAParameterOnly() throws Exception {
        TestThat.theInput(""
                + "{@snip:transform actions=trim,number kill=^\\d+.\\s*$ margin=3 start=10 step=10\n"
                + "       \n" // empty line will be killed by default
                + "       papirus mapirus\n"
                + "         tabbed in hamarin\n"
                + "       \n" // empty line will be killed by default
                + "       tabbed in hamarine\n"
                + "}"
        ).results(""
                + "20.    papirus mapirus\n"
                + "30.      tabbed in hamarin\n"
                + "50.    tabbed in hamarine\n");
    }

    @Test
    @DisplayName("snip:transform - works with empty actions")
    void testKillParameterOnly() throws Exception {
        TestThat.theInput(""
                + "{@snip:transform kill=A\n"
                + "Apple\n"
                + "Bpple\n"
                + "}"
        ).results(""
                + "Bpple\n"
        );
    }

    @Test
    @DisplayName("snip:transform - simple number and trim, kill auto added by keep parameter")
    void testKeepCanBeUsedAsAParameterOnly() throws Exception {
        TestThat.theInput(""
                + "{@snip:transform actions=trim,number kill=^\\d+.\\s*$ margin=3 start=10 step=10\n"
                + "       \n" // empty line will be killed by default
                + "       papirus mapirus\n"
                + "         tabbed in hamarin\n"
                + "       \n" // empty line will be killed by default
                + "       tabbed in hamarine\n"
                + "}"
        ).results(""
                + "20.    papirus mapirus\n"
                + "30.      tabbed in hamarin\n"
                + "50.    tabbed in hamarine\n");
    }

    @Test
    @DisplayName("snip:transform - simple kill and trim, number auto added")
    void testPatternCanNotBeUsedAsAParameterOnly() throws Exception {
        TestThat.theInput(""
                + "{@snip:transform actions=trim,number pattern=^\\d+.\\s*$ margin=3 start=10 step=10\n"
                + "       \n" // empty line will be killed by default
                + "       papirus mapirus\n"
                + "         tabbed in hamarin\n"
                + "       \n" // empty line will be killed by default
                + "       tabbed in hamarine\n"
                + "}"
        ).throwsBadSyntax("'pattern' can be used only when 'kill' specified as action or parameter.");
    }

    @Test
    @DisplayName("snip:transform - unnokwn action throws BadSyntax")
    void testUnknownAction() throws Exception {
        TestThat.theInput(""
                + "{@snip:transform actions=vandalize \n"
                + " just some text to be vandalized}"
        ).throwsBadSyntax("Unknown action 'vandalize'");
    }

    @Test
    @DisplayName("snip:transform - duplicate action throws BadSyntax")
    void testDuplicateAction() throws Exception {
        TestThat.theInput(""
                + "{@snip:transform actions=trim,trim \n"
                + " just some text to be trimmed twice}"
        ).throwsBadSyntax("Duplicate action\\(s\\) in trim,trim");
    }

    @Nested
    @DisplayName("All the tests of TestKillLines invoked through snip:transform")
    class TestKillLines {

        @Test
        @DisplayName("kill the lines by default that contain only space")
        void killEmptyLines() throws Exception {
            TestThat.theInput("{@snip:transform actions=kill \n" +
                    "\na\n\na\n\n\na\n\n" +
                    "}").results("a\na\na\n");
        }

        @Test
        @DisplayName("kill the lines by default that contain pattern")
        void killMatchingLinesLines() throws Exception {
            TestThat.theInput("{#snip:transform actions=kill pattern=a+\n" +
                    "birce\n" +
                    "a\n" +
                    "hurca\n" +
                    "abba\n" +
                    "\n" +
                    "nousea\n" +
                    "aaaaaaaaa\n" +
                    "\n" +
                    "}").results(
                    "birce\n" +
                            "\n" +
                            "\n"

            );
        }

        @Test
        @DisplayName("does not fail when there is nothing to kill")
        void noKill() throws Exception {
            TestThat.theInput("{#snip:transform actions=kill pattern=abrakadabra\n" +
                            "birce\na\nhurca\nabba\n\nnousea\naaaaaaaaa\n\n}")
                    .results("birce\na\nhurca\nabba\n\nnousea\naaaaaaaaa\n\n");
        }

        @Test
        @DisplayName("keeps the last line when not killing any line")
        void noKillTerminatingNoNl() throws Exception {
            TestThat.theInput("{#snip:transform actions=kill pattern=abrakadabra\n" +
                            "birce\na\nhurca\nabba\n\nnousea\naaaaaaaaa}")
                    .results("birce\na\nhurca\nabba\n\nnousea\naaaaaaaaa");
        }
    }

    @Nested
    @DisplayName("All the tests of TestSkipLines invoked through snip:transform")
    class TestSkipLines {

        @Test
        void testLineSkippingHappyPath() throws Exception {
            TestThat.theInput("{@snip:transform action=skip \n" +
                    "this line is included\n" +
                    "// skip something does not matter, this line is skipped\n" +
                    "so is this line\n" +
                    "and this\n" +
                    "there can be anything before the 'end    skip' and also after it and this is the last line skipped\n" +
                    "this is included\n" +
                    "}"
            ).results(
                    "this line is included\n" +
                            "this is included\n"
            );
        }

        @Test
        void testLineSkippingMultipleSegments() throws Exception {
            TestThat.theInput("{@snip:transform action=skip \n" +
                    "this line is included\n" +
                    "// skip something does not matter, this line is skipped\n" +
                    "so is this line\n" +
                    "and this\n" +
                    "there can be anything before the 'end    skip' and also after it and this is the last line skipped\n" +
                    "this is included\n" +
                    "skip\n" +
                    "ignored\n" +
                    "end skip" +
                    "}"
            ).results(
                    "this line is included\n" +
                            "this is included\n"
            );
        }

        @Test
        void testLineSkippingUnterminatedLastLine() throws Exception {
            TestThat.theInput("{@snip:transform action=skip \n" +
                    "this line is included\n" +
                    "// skip something does not matter, this line is skipped\n" +
                    "so is this line\n" +
                    "and this\n" +
                    "there can be anything before the 'end    skip' and also after it and this is the last line skipped\n" +
                    "this is included" +
                    "}"
            ).results(
                    "this line is included\n" +
                            "this is included" // there is no \n at the end!!!!
            );
        }

        @Test
        void testLineSkippingUnterminated() throws Exception {
            TestThat.theInput("{@snip:transform action=skip \n" +
                    "this line is included\n" +
                    "// skip something does not matter, this line is skipped\n" +
                    "so is this line\n" +
                    "and this\n" +
                    "}"
            ).results(
                    "this line is included\n"
            );
        }


        @Test
        void testLineSkippingAllLinesEmptyOutput() throws Exception {
            TestThat.theInput("{#snip:transform action=skip skip=hophop endSkip=pohpoh\n" +
                    "this line is included\n" +
                    "// hophop something does not matter, this line is hophopped\n" +
                    "so is this line\n" +
                    "and this\n" +
                    "there can be anything before the 'pohpoh' and also after it and this is the last line skipped\n" +
                    "this is included\n" +
                    "}"
            ).results(
                    "this line is included\n" +
                            "this is included\n");
        }

        @Test
        void testLineSkippingHappyPathWithDefinedStartAndStop() throws Exception {
            TestThat.theInput("{@snip:transform action=skip \n" +
                    "skip\n" +
                    "this line is there\n" +
                    "jump start here\n" +
                    "this line is skipped\n" +
                    "this line is skipped again\n" +
                    "land                 here\n" +
                    "there can be more lines\n" +
                    "}"
            ).results("");
        }
    }

    @Nested
    @DisplayName("All the tests of TestReplaceLines invoked through snip:transform")
    class TestReplaceLines {

        @Test
        void testReplaceLinesWithRegex() throws Exception {
            TestThat.theInput("" +
                    "{@snip:transform action=replace replace=\"/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean\"\n" +
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
                    "{@snip:transform action=replace replace=/^appl(.)/p$1ar/ replace=/^fox/whale/ replace=\"/win(.)e(.) //\" replace=/f(.)rest/$1cean\n" +
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
                    "{@snip:transform action=replace detectNoChange replace=\"/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean\"\n" +
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
                    "{@snip:transform action=replace detectNoChange replace=\"/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean\"\n" +
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
                    "{@snip:transform action=replace detectNoChange replace=\"/abra/kadabra\"\n" +
                            "zummm" +
                            "}").throwsBadSyntax();
        }

        @Test
        void testReplaceLinesWithRegexDeleting() throws Exception {
            TestThat.theInput(
                    "{@snip:transform action=replace replace=\"/^appl(.)/p$1ar/^fox/whale/win(.)e(.) //f(.)rest/$1cean/a\"\n" +
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
                    "{@snip:transform action=replace replace=\"\"\n}").throwsBadSyntax();
        }

        @Test
        void testReplaceLinesWithBadRegex() throws Exception {
            TestThat.theInput("{@snip:transform action=replace replace=\"/(/\"\n}").throwsBadSyntax();
        }
    }


    @Nested
    @DisplayName("All the tests of TestTrimming invoked through snip:transform")
    class TestTrimming {

        @Test
        @DisplayName("When there is no newLine after 'trimLines' nothing is trimmed")
        void trimsToLeftNothing() throws Exception {
            TestThat.theInput("{@snip:transform action=trim \nwupbaba\n" +
                    "        siiias das\n" +
                    "sdsd sdsd ddd" +
                    "}").results("wupbaba\n" +
                    "        siiias das\n" +
                    "sdsd sdsd ddd");
        }

        @Test
        @DisplayName("Trims the lines to the left")
        void trimsToLeftNeatly() throws Exception {
            TestThat.theInput("{@snip:transform action=trim\n" +
                    "      for i in range(1,211):\n" +
                    "        print \"hi\"\n" +
                    "        print \"low\"" +
                    "}").results("for i in range(1,211):\n" +
                    "  print \"hi\"\n" +
                    "  print \"low\"");
        }


        @Test
        @DisplayName("Trims also vertical deleting empty lines from the start and from the end")
        void trimsVertical() throws Exception {
            TestThat.theInput("{#snip:transform action=trim trimVertical\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "      for i in range(1,211):\n" +
                    "        print \"hi\"\n" +
                    "        print \"low\"\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "}").results("for i in range(1,211):\n" +
                    "  print \"hi\"\n" +
                    "  print \"low\""
            );
        }

        @Test
        @DisplayName("Trims also vertical deleting empty lines from the start and from the end")
        void trimsVerticalWithOption() throws Exception {
            TestThat.theInput("{#snip:transform action=trim trimVertical" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "      for i in range(1,211):\n" +
                    "        print \"hi\"\n" +
                    "        print \"low\"\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "}").results("for i in range(1,211):\n" +
                    "  print \"hi\"\n" +
                    "  print \"low\""
            );
        }

        @Test
        @DisplayName("Trims only vertical deleting empty lines from the start and from the end")
        void trimsVerticalOnlyWithOption() throws Exception {
            TestThat.theInput("{#snip:transform action=trim verticalTrimOnly" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "      for i in range(1,211):\n" +
                    "        print \"hi\"\n" +
                    "        print \"low\"\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "}").results("      for i in range(1,211):\n" +
                    "        print \"hi\"\n" +
                    "        print \"low\""
            );
        }

        @Test
        @DisplayName("Empties the lines that contain only spaces")
        void trimsEmptySpaceLines() throws Exception {
            TestThat.theInput("{@snip:transform action=trim                                    \n" +
                    "                     \n" +
                    "      for i in range(1,211):               \n" +
                    "                               \n" +
                    "        print \"hi\"\n" +
                    "        print \"low\"" +
                    "}").results(
                    "\n" +
                            "for i in range(1,211):\n" +
                            "\n" +
                            "  print \"hi\"\n" +
                            "  print \"low\"");
        }

        @Test
        @DisplayName("Trims the lines with margin")
        void trimsToLeftWithMargin() throws Exception {
            TestThat.theInput("{#snip:transform action=trim margin=1\n" +
                    "      for i in range(1,211):\n" +
                    "        print \"hi\"\n" +
                    "        print \"low\"" +
                    "}").results(" for i in range(1,211):\n" +
                    "   print \"hi\"\n" +
                    "   print \"low\"");
        }

        @Test
        @DisplayName("When needed inserts space to have the margin")
        void addsMargin() throws Exception {
            TestThat.theInput("{#snip:transform action=trim margin=1\n" +
                    "for i in range(1,211):\n" +
                    "  print \"hi\"\n" +
                    "  print \"low\"" +
                    "}").results(" for i in range(1,211):\n" +
                    "   print \"hi\"\n" +
                    "   print \"low\"");
        }
    }

    @Nested
    @DisplayName("All the untabbing tests")
    class TestUntab {
        @Test
        @DisplayName("Replaces the tabs properly")
        void replacesTabs() throws Exception {
            TestThat.theInput("{@snip:transform action=trim tab=8\n" +
                            "..\t.\t.}"
                    //          01234567890123456
            ).results("..      .       .");
        }

        @Test
        @DisplayName("tab value can only be positive")
        void onlyPositiveTabs() throws Exception {
            TestThat.theInput("{@snip:transform action=trim tab=0\n" +
                    "..\t.\t.}"
            ).throwsBadSyntax("The tab size must be greater than zero");
        }
    }

    @Nested
    @DisplayName("All the tests of TestReflow invoked through snip:transform")
    class TestReflow {

        @Test
        void testReflow() throws Exception {
            TestThat.theInput(
                    "{@define width=11}{@snip:transform action=reflow width=10\n" +
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
                    "{@snip:transform action=reflow \n" +
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
                    "{@snip:transform action=reflow \n" +
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

    @Nested
    @DisplayName("All the tests of TestNumberLines invoked through snip:transform")
    class TestNumberLines {

        @Test
        @DisplayName("Simple numbering the lines")
        void numberLines() throws Exception {
            TestThat.theInput("{@snip:transform action=number \n" +
                            "line one\n" +
                            "line two\n" +
                            "line three}\n"
                    )
                    .results("1. line one\n" +
                            "2. line two\n" +
                            "3. line three\n");
        }

        @Test
        @DisplayName("Simple numbering the lines using formatting")
        void numberLinesFormatted() throws Exception {
            TestThat.theInput("{#snip:transform action=number format=\"%03d: \"\n" +
                            "line one\n" +
                            "line two\n" +
                            "line three}"
                    )
                    .results("001: line one\n" +
                            "002: line two\n" +
                            "003: line three");
        }

        @Test
        @DisplayName("When the format string is wrong it throws BadSyntax and not something else")
        void throwsBadSyntaxForWrongFormatting() throws Exception {
            TestThat.theInput("{#snip:transform action=number format=\"%03: \"\n" +
                    "line one\n" +
                    "line two\n" +
                    "line three}\n"
            ).throwsBadSyntax();
        }

        @Test
        @DisplayName("Simple numbering the lines starting at ten")
        void numberLinesStartFromTen() throws Exception {
            TestThat.theInput("{#snip:transform action=number start=10\n" +
                            "line one\n" +
                            "line two\n" +
                            "line three}\n"
                    )
                    .results("10. line one\n" +
                            "11. line two\n" +
                            "12. line three\n");
        }

        @Test
        @DisplayName("Simple numbering the lines stepping by ten")
        void numberLinesSteppingTen() throws Exception {
            TestThat.theInput("{#snip:transform action=number step=10 \n" +
                            "line one\n" +
                            "line two\n" +
                            "line three}\n"
                    )
                    .results("1. line one\n" +
                            "11. line two\n" +
                            "21. line three\n");

        }
    }

    @Nested
    @DisplayName("All the tests of range invoked through snip:transform")
    class TestRange {
        @Test
        @DisplayName("A range with no specification returns all the lines")
        void testNoRangeNoOp() throws Exception {
            TestThat.theInput("{@snip:transform action=range\n" +
                    "1\n" +
                    "2\n" +
                    "3\n" +
                    "4\n" +
                    "}").results("1\n2\n3\n4\n");
        }

        @Test
        @DisplayName("A transform range with lines returns one range")
        void testOneRange() throws Exception {
            TestThat.theInput("{@snip:transform action=range lines=1..2\n" +
                    "1\n" +
                    "2\n" +
                    "3\n" +
                    "4\n" +
                    "}").results("1\n2\n");
        }

        @Test
        @DisplayName("transform ranges' with lines returns the ranges")
        void testTwoRanges() throws Exception {
            TestThat.theInput("{@snip:transform action=ranges lines=1..2;2..3\n" +
                    "1\n" +
                    "2\n" +
                    "3\n" +
                    "4\n" +
                    "}").results("1\n2\n2\n3\n");
        }

        @Test
        @DisplayName("transform range preserves the last new line")
        void testTrailingNoNewLine1() throws Exception {
            TestThat.theInput("{@snip:transform action=ranges lines=1..4\n" +
                    "1\n" +
                    "2\n" +
                    "3\n" +
                    "4}").results("1\n2\n3\n4");
        }

        @Test
        @DisplayName("transform range preserves the last new line")
        void testTrailingNoNewLine2() throws Exception {
            TestThat.theInput("{@snip:transform lines=1..4,4\n" +
                    "1\n" +
                    "2\n" +
                    "3\n" +
                    "4}").results("1\n2\n3\n4\n4");
        }
    }
}
