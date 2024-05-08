package javax0.jamal.snippet;

import javax0.jamal.api.Position;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TestPos {

    @Nested
    class SuccessCases {
        /*
         * The reported position is the column of the character following the name of the macro characters.
         * In this test
         *
         * .1937
         *    .1940     .1950     .1960
         * 789012345678901234567890123456
         * {@pos.file}:{@pos.line}:{@pos.column}
         *
         * The actual column numbers in the test were selected as the year 1937 was when my grandparents married and 1966
         * was the year when I was born. That way the span of the macros in this test represent the childhood of my father.
         */
        @Test
        @DisplayName("Simple pos test")
        void testSimpleSample() throws Exception {
            TestThat.theInput("{@pos.file}:{@pos.line}:{@pos.column}").atPosition("wupsy", 666, 1937).results("wupsy:666:1966");
        }

        @Test
        @DisplayName("Simple pos test with some spaces")
        void testSimpleSample2() throws Exception {
            TestThat.theInput("{@pos .file}:{@pos .line}:{@pos .column}").atPosition("wupsy", 666, 1937).results("wupsy:666:1968");
        }

        @Test
        @DisplayName("Formatted default pos test")
        void testFormattedDefaultSample() throws Exception {
            TestThat.theInput("{@pos}").atPosition("wupsy", 666, 1937).results("wupsy:666:1942");
        }

        @Test
        @DisplayName("Formatted pos test")
        void testFormattedSample() throws Exception {
            TestThat.theInput("{@pos (format=\"%f|%l|%c\")}").atPosition("wupsy", 666, 1937).results("wupsy|666|1942");
        }

        @Test
        @DisplayName("Parent pos test")
        void testParentSample() throws Exception {
            TestThat.theInput("{@pos (parent format=\"%f|%l|%c\")}")
                    .atPosition(new Position("mipsi", 1, 3, new Position("wupsy", 666, 1937)))
                    .results("wupsy|666|1937");
        }

        @Test
        @DisplayName("Top pos test")
        void testTopSample() throws Exception {
            TestThat.theInput("{@pos (top format=\"%f|%l|%c\")}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, -70,
                                    new Position("wupsy", 666, 1937))))
                    .results("wupsy|666|1937");
        }

        @Test
        @DisplayName("up=0 pos test")
        void testUp0Sample() throws Exception {
            TestThat.theInput("{@pos (up=0 format=\"%f|%l|%c\")}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, -70,
                                    new Position("wupsy", 666, 1937))))
                    .results("mipsi|1|8");
        }

        @Test
        @DisplayName("up=1 pos test")
        void testUp1Sample() throws Exception {
            TestThat.theInput("{@pos (up=1 format=\"%f|%l|%c\")}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, 70,
                                    new Position("wupsy", 666, 1937))))
                    .results("baksi|1976|70");
        }

        @Test
        @DisplayName("up=2 pos test")
        void testUp2Sample() throws Exception {
            TestThat.theInput("{@pos (up=2 format=\"%f|%l|%c\")}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, -70,
                                    new Position("wupsy", 666, 1937))))
                    .results("wupsy|666|1937");
        }

        @Test
        @DisplayName("test with windows sample")
        void testWindowsSample() throws Exception {
            TestThat.theInput("{@pos.file}")
                    .atPosition(new Position("A:\\floppy_drive\\64kb.adoc.jam", 1, 1))
                    .results("A:\\floppy_drive\\64kb.adoc.jam");
        }

        @Test
        @DisplayName("parop \"all\" pos test")
        void testAllSample() throws Exception {
            TestThat.theInput("{@pos (all format=\"%f|%l|%c\")}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, -70,
                                    new Position("wupsy", 666, 1937))))
                    .results("mipsi|1|8,baksi|1976|-70,wupsy|666|1937");
        }
    }

    @Nested
    class FailuresExceptionsErrors {
        @Test
        @DisplayName("up too much")
        void testUp2Sample() throws Exception {
            TestThat.theInput("{@pos (up=9999999 format=\"%f|%l|%c\")}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, -70,
                                    new Position("wupsy", 666, 1937))))
                    .throwsBadSyntax("The value 9999999 for up in macro pos is too large, there are not so many levels of hierarchy.");
        }

        @Test
        @DisplayName("options mixup")
        void mixupInOptions() throws Exception {
            TestThat.theInput("{@pos (up=1 top format=\"%f|%l|%c\")}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, -70,
                                    new Position("wupsy", 666, 1937))))
                    .throwsBadSyntax("Macro 'pos' can handle one 'top', 'parent', 'all' or 'up' parameter only\\. They cannot be used together");
        }

        @Test
        @DisplayName("format with .xxx is error")
        void formatWithDotX() throws Exception {
            TestThat.theInput("{@pos (format=\"%f|%l|%c\").anything}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, -70,
                                    new Position("wupsy", 666, 1937))))
                    .throwsBadSyntax("Cannot use 'format' for 'pos' when there is \\.file, \\.line, or \\.column");
        }

        @Test
        @DisplayName("sep without all logs warning only")
        void sepWoAll() throws Exception {
            TestThat.theInput("{@pos (sep=\"*\" top)}")
                    .atPosition(new Position("mipsi", 1, 3,
                            new Position("baksi", 1976, -70,
                                    new Position("wupsy", 666, 1937))))
                    .results("wupsy:666:1937");
        }


    }

}
