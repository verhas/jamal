package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TestVariation {

    @Nested
    class GoodCases {
        @Test
        @DisplayName("Repeated text when identical is okay")
        void testPastedOK() throws Exception {
            TestThat.theInput("{@variation (id=foo)abraka dabra} {@variation (id=foo)abraka dabra}")
                    .results("abraka dabra abraka dabra");
        }

        @Test
        @DisplayName("Repeated text when identical is okay with ignoreCase")
        void testPastedOKIgnoreCase() throws Exception {
            TestThat.theInput("{@variation (id=foo ignoreCase)abraka dabra} {@variation (id=foo ignoreCase)ABRAKA DABRA}")
                    .results("abraka dabra ABRAKA DABRA");
        }

        @Test
        @DisplayName("Repeated text when identical is okay when spaces are not interesting")
        void testPastedOKWithSpaces() throws Exception {
            TestThat.theInput("{@variation (id=foo ignoreSpace)abraka dabra} {@variation (id=foo ignoreSpace)abraka\n" +
                            "dabra}")
                    .results("abraka dabra abraka\ndabra");
        }

        @Test
        @DisplayName("Repeated text when identical when only protected part is different")
        void testPastedOKWithProtectedPart() throws Exception {
            TestThat.theInput("{@variation (id=foo)abraka <<a>> dabra} {@variation (id=foo ignoreSpace)abraka <<z>> dabra}")
                    .results("abraka a dabra abraka z dabra");
        }

        @Test
        @DisplayName("Repeated text when identical when only protected part is missing")
        void testPastedOKWithProtectedPartMissing() throws Exception {
            TestThat.theInput("{@variation (id=foo)abraka <<a>> dabra} {@variation (id=foo ignoreSpace)abraka dabra}")
                    .results("abraka a dabra abraka dabra");
        }

        @Test
        @DisplayName("Repeated text when identical when only protected parts are different")
        void testPastedOKWithProtectedParts() throws Exception {
            TestThat.theInput("{@variation (id=foo)<<abrak>>a <<a>> dabra} {@variation (id=foo)<<mubarak>>a <<z>> dabra}")
                    .results("abraka a dabra mubaraka z dabra");
        }

        @Test
        @DisplayName("Repeated text when identical when only protected parts are different with specified start and end")
        void testPastedOKWithProtectedPartsParoprStartEnd() throws Exception {
            TestThat.theInput("{@variation (id=foo)<<abrak>>a <<a>> dabra} {@variation (id=foo start=< end=>)<mubarak>a <z> dabra}")
                    .results("abraka a dabra mubaraka z dabra");
        }

        @Test
        @DisplayName("start and end is inherited from the first definition")
        void testStartEndInherit() throws Exception {
            TestThat.theInput("{@variation (id=foo start=< end=>)<abrak>a <a> dabra} {@variation (id=foo)<mubarak>a <z> dabra}")
                    .results("abraka a dabra mubaraka z dabra");
        }

        @Test
        @DisplayName("start and end is global")
        void testStartEndGloval() throws Exception {
            TestThat.theInput("{@variation (id=wuff) Here the <<variable>> part uses the default}\n" +
                            "{@define variation$start=[}" +
                            "{@define variation$end=]}" +
                            "{@variation (id=wuff) Here the <<changing>> part uses the default}\n" +
                            "{@variation (id=quack) Here the [variable] part uses the newly defined}\n" +
                            "{@variation (id=quack) Here the [changling] part uses the newly defined}")
                    .results(" Here the variable part uses the default\n" +
                            " Here the changing part uses the default\n" +
                            " Here the variable part uses the newly defined\n" +
                            " Here the changling part uses the newly defined");
        }
    }

    @Nested
    class FailCases {
        @Test
        @DisplayName("Repeated text when differs in space throws up")
        void testPastedBadWithSpaces() throws Exception {
            TestThat.theInput("{@variation (id=foo ignoreSpace)abraka dabra} {@variation (id=foo)abr ak a dabra}")
                    .throwsBadSyntax("The copy/paste text 'foo' was already defined and the content is different\\.");
        }

        @Test
        @DisplayName("It is an error to use a pasted macro only once")
        void testErrorUsingOnlyOnce() throws Exception {
            TestThat.theInput("{@variation (id=foo)abraka dabra}")
                    .throwsBadSyntax("The pasted macro 'foo' was used only once\\.");
        }

        @Test
        @DisplayName("It is an error when protected part overflows")
        void testErrorProtectedOverflow() throws Exception {
            TestThat.theInput("{@variation (id=foo)abraka <<a> dabra}{@variation (id=foo)abraka <<a> dabra}")
                    .throwsBadSyntax("Protected part starting with '<<' is not closed with '>>' in.*");
        }

        @Test
        @DisplayName("Error to have unpaired >>")
        void testBadBracketing1() throws Exception {
            TestThat.theInput("{@variation (id=foo)abraka <<a>> >> dabra}{@variation (id=foo)abraka <<a>> >> dabra}")
                    .throwsBadSyntax("There is a superfluous '>>' in the input string.*");
        }

        @Test
        @DisplayName("Error to have unpaired << ")
        void testBadBracketing2() throws Exception {
            TestThat.theInput("{@variation (id=foo)abraka <<a<< >> dabra}{@variation (id=foo)abraka << <<a>> dabra}")
                    .throwsBadSyntax("There is a superfluous '<<' in the input string.*");
        }

    }

}
