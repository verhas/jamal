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
                + "b#a#d#c}"
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
        ).results("a#b#c#d");
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
    @DisplayName("sort - sort based on pattern using only parts of it")
    void testSortWithPatternGroup() throws Exception {
        TestThat.theInput(""
                + "{@sort pattern=\\((.*)\\)\n"
                + "(aa)zzzzzz\n"
                + "zzzz(dd)\n"
                + "zzzzzzzz(bb)\n"
                + "zzzzzzzzzz(cc)}"
        ).results("(aa)zzzzzz\n"
                + "zzzzzzzz(bb)\n"
                + "zzzzzzzzzz(cc)\n"
                + "zzzz(dd)");
    }

    @Test
    @DisplayName("sort - sort based on pattern using multiple parts of it")
    void testSortWithPatternGroups() throws Exception {
        TestThat.theInput(""
                + "{@sort pattern=\\((.*)\\).*\\[(.*)\\]\n"
                + "(a)zzz[a]zzz\n"
                + "zzzz(dd)sasa[13]\n"
                + "zzzzzzzz(bb)[]\n"
                + "zzzzzzzzzz(b)[c]}"
        ).results("(a)zzz[a]zzz\n"
                + "zzzzzzzz(bb)[]\n"
                + "zzzzzzzzzz(b)[c]\n"
                + "zzzz(dd)sasa[13]");
    }

    @Test
    @DisplayName("sort - sort based on pattern that sometimes does not match the line")
    void testSortWithPatternNotMatching() throws Exception {
        TestThat.theInput(""
                + "{@sort pattern=[a-f]{2}\n"
                + "azzzzzz\n"
                + "azzzzzzaa\n"
                + "azzzzzzba\n"
                + "zzzzdd\n"
                + "zzzzzzzzbb\n"
                + "zzzzzzzzzzcc}"
        ).results("azzzzzzaa\n" +
                "azzzzzz\n" +
                "azzzzzzba\n" +
                "zzzzzzzzbb\n" +
                "zzzzzzzzzzcc\n" +
                "zzzzdd");
    }

    @Test
    @DisplayName("sort - sort based on columns")
    void testSortWithColumns() throws Exception {
        TestThat.theInput(""
                + "{@sort columns=3..5\n"
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
        @DisplayName("Column indexing error")
        void errorsWhenStringIsShort() throws Exception{
            TestThat.theInput(""
                    + "{#sort columns=1..3\n"
                    + "b\n"
                    + "a\n"
                    + "d\n"
                    + "c"
                    + "}"
            ).throwsBadSyntax("Column specification does not fit the lines");
        }

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
        @DisplayName("sort - sorts wrong numbers")
        void testSortWithWromngNumeric() throws Exception {
            TestThat.theInput(""
                    + "{#sort numeric\n"
                    + "192.168.0.13\n"
                    + "alma\n"
                    + "1\n"
                    + "2"
                    + "}"
            ).throwsBadSyntax("Numeric sorting on non numeric values");
        }

        @Test
        @DisplayName("columns cannot have multiple ranges")
        void cantSortWithBadColumns() throws Exception {
            TestThat.theInput(""
                    + "{#sort columns=2..4,5..6\n"
                    + "aaaaaaaaaab\n"
                    + "aaaaaaaaaaa\n"
                    + "aaaaaaaaaad\n"
                    + "aaaaaaaaaac"
                    + "}"
            ).throwsBadSyntax("The option 'columns' can only have a single range value!");
        }

    }


}
