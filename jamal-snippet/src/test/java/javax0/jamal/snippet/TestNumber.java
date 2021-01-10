package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestNumber {

    @Test
    @DisplayName("Simple numbering the lines")
    void numberLines() throws Exception {
        TestThat.theInput("{@numberLines line one\n" +
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
        TestThat.theInput("{#numberLines line one\n" +
            "line two\n{@define format=%03d: }" +
            "line three}"
        )
            .results("001: line one\n" +
                "002: line two\n" +
                "003: line three");
    }

    @Test
    @DisplayName("When the format string is wrong it throws BadSyntax and not something else")
    void throwsBadSyntaxForWrongFormatting() throws Exception {
        TestThat.theInput("{#numberLines line one\n" +
            "line two\n{@define format=%03: }" +
            "line three}\n"
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("Simple numbering the lines starting at ten")
    void numberLinesStartFromTen() throws Exception {
        TestThat.theInput("{#numberLines line one\n" +
            "line two\n{@define start=10}" +
            "line three}\n"
        )
            .results("10. line one\n" +
                "11. line two\n" +
                "12. line three\n");
    }

    @Test
    @DisplayName("Simple numbering the lines stepping by ten")
    void numberLinesSteppingTen() throws Exception {
        TestThat.theInput("{#numberLines line one\n" +
            "line two\n{@define step=10}" +
            "line three}\n"
        )
            .results("1. line one\n" +
                "11. line two\n" +
                "21. line three\n");

    }
}
