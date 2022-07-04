package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TestSort {

    @Test
    @DisplayName("sort - simple sorting")
    void testSort() throws Exception {
        TestThat.theInput(""
                + "{@sort\n"
                + "b\n"
                + "a\n"
                + "d\n"
                + "c}"
        ).results("a\n"
                + "b\n"
                + "c\n"
                + "d");
    }

    @Test
    @DisplayName("sort - sorts based on separator")
    void testSortWithSeparator() throws Exception {
        TestThat.theInput(""
                + "{@sort separator=#\n"
                + "b#"
                + "a#"
                + "d#"
                + "c}"
        ).results("a\n"
                + "b\n"
                + "c\n"
                + "d");
    }

    @Test
    @DisplayName("sort - joins lines with join")
    void testSortWithJoin() throws Exception {
        TestThat.theInput(""
                + "{@sort join=#\n"
                + "b\n"
                + "a\n"
                + "d\n"
                + "c}"
        ).results("a#"
                + "b#"
                + "c#"
                + "d");
    }

    @Test
    @DisplayName("sort - sort based on locale")
    void testSortWithLocale() throws Exception {
        TestThat.theInput(""
                + "{@sort locale=HU\n"
                + "a\n"
                + "á\n"
                + "d\n"
                + "b}"
        ).results("a\n"
                + "á\n"
                + "b\n"
                + "d");
    }

    @Test
    @DisplayName("sort - sort based on pattern")
    void testSortWithPattern() throws Exception {
        TestThat.theInput(""
                + "{@sort pattern=[a-f]{2}\n"
                + "zzzzzzaa\n"
                + "zzzzdd\n"
                + "zzzzzzzzbb\n"
                + "zzzzzzzzzzcc}"
        ).results("zzzzzzaa\n"
                + "zzzzzzzzbb\n"
                + "zzzzzzzzzzcc\n"
                + "zzzzdd");
    }

    @Test
    @DisplayName("sort - sort based on columns")
    void testSortWithColumns() throws Exception {
        TestThat.theInput(""
                + "{@sort columns=2..4\n"
                + "zzabzzaa\n"
                + "zzaadd\n"
                + "zzcazzzzbb\n"
                + "zzbczzzzzzcc}"
        ).results("zzaadd\n"
                + "zzabzzaa\n"
                + "zzbczzzzzzcc\n"
                + "zzcazzzzbb");
    }

    @Test
    @DisplayName("sort - sorts numbers")
    void testSortWithNumeric() throws Exception {
        TestThat.theInput(""
                + "{#sort numeric\n"
                + "11\n"
                + "10\n"
                + "1\n"
                + "2"
                + "}"
        ).results("1\n"
                + "2\n"
                + "10\n"
                + "11");
    }

    @Test
    @DisplayName("sort - sorts in reverse")
    void testSortWithReverse() throws Exception {
        TestThat.theInput(""
                + "{#sort reverse\n"
                + "b\n"
                + "a\n"
                + "d\n"
                + "c"
                + "}"
        ).results("d\n"
                + "c\n"
                + "b\n"
                + "a");
    }

    @Nested
    @DisplayName("Failure cases")
    class TestSortFailure {

        @Test
        @DisplayName("pattern & columns together is bad syntax")
        void cantSortWithColumnsAndPattern() throws Exception {
            TestThat.theInput(""
                    + "{#sort columns=1,2 pattern=[a-z]\n"
                    + "b\n"
                    + "a\n"
                    + "d\n"
                    + "c"
                    + "}"
            ).throwsBadSyntax("Can not use both options 'pattern' and 'columns' together.");
        }

        @Test
        @DisplayName("pattern that can't compile is bad syntax")
        void cantSortWithBadPattern() throws Exception {
            TestThat.theInput(""
                    + "{#sort pattern=[a-z)\n"
                    + "b\n"
                    + "a\n"
                    + "d\n"
                    + "c"
                    + "}"
            ).throwsBadSyntax("There was an exception converting the parameter 'pattern'.*");
        }

        @Test
        @DisplayName("pattern that can't compile is bad syntax")
        void cantSortWithBadColumns() throws Exception {
            TestThat.theInput(""
                    + "{#sort columns=2..4,5..6\n"
                    + "aaaaaaaaaab\n"
                    + "aaaaaaaaaaa\n"
                    + "aaaaaaaaaad\n"
                    + "aaaaaaaaaac"
                    + "}"
            ).throwsBadSyntax("The option '.*' can only have a single range value!");
        }

    }


}
