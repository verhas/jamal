package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestSippets {

    @Test
    @DisplayName("Lines are numbered using the default values")
    public void testLineNumbering() throws Exception {
        var numberLines = TestThat.theMacro(SnippetMacros.Number.class);
        numberLines.fromTheInput("line 1\n" +
            "line 2\n" +
            "line 3\n").results("1. line 1\n" +
            "2. line 2\n" +
            "3. line 3\n");
    }

    @Test
    @DisplayName("Lines are numbered using the values set inside")
    public void testLineNumberingSet() throws Exception {
        TestThat.theInput(
            "{@sep (( ))}\\\n"+
            "((@import res:SnippetMacros.jim))\\\n" +
                "((#number ((@define start=55))((@define step=2))((@define format=%03d. ))  \n" +
                "line 1\n" +
                "line 2\n" +
                "line 3))\n").results("055. line 1\n" +
            "057. line 2\n" +
            "059. line 3\n");
    }

    @Test
    public void testTrimming() throws Exception {
        var numberLines = TestThat.theMacro(SnippetMacros.Trim.class);
        numberLines.fromTheInput("\n   3spaces\n" +
            "  2 spaces\n" +
            "    4 spaces\n").results(" 3spaces\n" +
            "2 spaces\n" +
            "  4 spaces\n");
    }
}
