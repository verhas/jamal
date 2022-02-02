package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUntab {

    @Test
    @DisplayName("Replaces the tabs properly")
    void replacesTabs() throws Exception {
        TestThat.theInput("" +
                        "{@untab tab=8\n" +
                        "..\t.\t.}"
                //          01234567890123456
        ).results("..      .       .");
    }

    @Test
    @DisplayName("tab value can only be positive")
    void onlyPositiveTabs() throws Exception {
        TestThat.theInput("{@untab tab=0\n" +
                "..\t.\t.}"
        ).throwsBadSyntax("The tab size must be greater than zero");
    }

    @Test
    @DisplayName("Sample unit test imported into the documentation as snippet")
    void sample() throws Exception {
        TestThat.theInput("" +
                        // snippet untabSample
                        "{@untab tabSize=8\n" +
                        ".......|.......|.......|.......|\n" +
                        "...\t... .   .\t.. \t." +
                        "}"
                // end snippet
        ).results("" +
                        // snippet untabSampleOutput
                        ".......|.......|.......|.......|\n" +
                        "...     ... .   .       ..      ."
                // end snippet
        );
    }
}
