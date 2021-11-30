package javax0.jamal.builtinstest;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestTopInclude {

    @Test
    @DisplayName("Include a file from the top.")
    void testIncludeTop() throws Exception {
        TestThat.theInput("{@include subdir/inc1.jim}").atPosition("res:anything", 1, 1).results("This is good");
    }

    @Test
    @DisplayName("Import a file from the top.")
    void testImportTop() throws Exception {
        TestThat.theInput("{@include subdir/imp1.jim}").atPosition("res:anything", 1, 1).results("This is good");
    }
}
