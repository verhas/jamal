package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestSkipLines {

    @Test
    void testLineSkippingHappyPath() throws Exception {
        TestThat.theInput("{@skipLines \n" +
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
        TestThat.theInput("{@skipLines \n" +
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
        TestThat.theInput("{@skipLines \n" +
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
        TestThat.theInput("{@skipLines \n" +
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
        TestThat.theInput("{#skipLines \n" +
            "{@define skip=hophop}{@define endSkip=pohpoh}" +
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
        TestThat.theInput("{@skipLines \n" +
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
