package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestSnipCheck {

    @Test
    @DisplayName("Throws exception when neither hash nor lines is specified")
    void norhashNeitherLine() throws Exception {
        TestThat.theInput("" +
            "{@snip:define wubaba=}" +
            "{@snip:check id=\"wubaba\"}"
        ).throwsBadSyntax("Neither lines, nor hash is checked in snip:check''");
    }

    @Test
    @DisplayName("Throws exception when both hash and lines are specified")
    void hashAndLine() throws Exception {
        TestThat.theInput("" +
            "{@snip:check hash=\"\" lines=2 id=\"wubaba\"}"
        ).throwsBadSyntax("You cannot specify 'lines' and 'hash' the same time for snip:check");
    }

    @Test
    @DisplayName("Throws exception when the lines value is wrong")
    void throwsForLineMismatch() throws Exception {
        TestThat.theInput("" +
            "{@snip:define wubaba=1\n" +
            "2\n" +
            "3}" +
            "{@snip:check id=\"wubaba\" lines=2}"
        ).throwsBadSyntax("The id\\(wubaba\\) has 3 lines and not 2\\..*");
    }

    @Test
    @DisplayName("Throws exception when the hash is short")
    void throwsForHashShort() throws Exception {
        TestThat.theInput("" +
            "{@snip:define wubaba=1\n" +
            "2\n" +
            "3}" +
            "{@snip:check id=\"wubaba\" hash=2}"
        ).throwsBadSyntax("The id\\(wubaba\\) hash is 'ad53e880\\.6d17c82d\\.38902738\\.d1d47d96\\.bddaade2\\.75134663\\.22efa0f7\\.93149dd0'\\. '2' is too short, you need at least 6 characters\\..*");
    }

    @Test
    @DisplayName("Throws exception when the hash is short and is not contained")
    void throwsForHashShortNotContained() throws Exception {
        TestThat.theInput("" +
            "{@snip:define wubaba=1\n" +
            "2\n" +
            "3}" +
            "{@snip:check id=\"wubaba\" hash=X}"
        ).throwsBadSyntax("The id\\(wubaba\\) hash is 'ad53e880\\.6d17c82d\\.38902738\\.d1d47d96\\.bddaade2\\.75134663\\.22efa0f7\\.93149dd0', not 'X', which is too short anyway, you need at least 6 characters\\..*");
    }

    @Test
    @DisplayName("Throws exception when the hash is wrong")
    void throwsForHashWrong() throws Exception {
        TestThat.theInput("" +
            "{@snip:define wubaba=1\n" +
            "2\n" +
            "3}" +
            "{@snip:check id=\"wubaba\" hash=ad53e9}"
        ).throwsBadSyntax("The id\\(wubaba\\) hash is 'ad53e880\\.6d17c82d\\.38902738\\.d1d47d96\\.bddaade2\\.75134663\\.22efa0f7\\.93149dd0' does not contain 'ad53e9'\\..*");
    }

    @Test
    @DisplayName("Check is ignored totally when the system property jamal.snippet.check=false")
    void ignoresWhenSystemPropertyIsSet() throws Exception {
        final var save = System.getProperty("jamal.snippet.check");
        System.setProperty("jamal.snippet.check", "false");
        TestThat.theInput("" +
            "{@snip:check hash=\"\" lines=2 id=\"wubaba\"}"
        ).results("");
        if (save == null) {
            System.clearProperty("jamal.snippet.check");
        } else {
            System.setProperty("jamal.snippet.check", save);
        }
    }

    @Test
    @DisplayName("Checks the number of lines in the snippet")
    void checksTheNumberOfLines() throws Exception {
        TestThat.theInput("" +
            "{@snip:define wubaba=1\n" +
            "2\n" +
            "3}" +
            "{@snip:check id=\"wubaba\" lines=3}" +
            "{@snip:clear}" +
            "{@snip:define wubaba=1\n" +
            "2\n" +
            "3\n}" +
            "{@snip:check id=\"wubaba\" lines=3}"
        ).results("");
    }

}
