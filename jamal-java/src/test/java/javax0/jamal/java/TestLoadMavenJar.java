package javax0.jamal.java;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test that the macro {@code maven:load} can load classes from a Jar file and the macros from the Jar file.
 */
public class TestLoadMavenJar {

    @DisplayName("Loads classes from Jar file but not the macros, which are already loaded")
    @Test
    void testLoadOnlyNewMacros() throws Exception {
        final var test = TestThat.theInput("{@try! {@array :2:0:1:2:3}}{@maven:load (new)  com.javax0.jamal:jamal-test:1.12.7-SNAPSHOT}{@array :2:0:1:2:3}");
        final var processor = test.getProcessor();
        final var dateBefore = processor.getRegister().getMacro("date").get();
        test.results("There is no built-in macro with the id 'array'; did you mean '@try'?2");
        final var dateAfter = processor.getRegister().getMacro("date").get();
        Assertions.assertSame(dateBefore, dateAfter);
    }

    @DisplayName("Fails loading when it finds a macro that was already loaded")
    @Test
    void testFailFindingOldMacros() throws Exception {
        TestThat.theInput("{@try! {@array :2:0:1:2:3}}{@maven:load com.javax0.jamal:jamal-test:1.12.7-SNAPSHOT}{@array :2:0:1:2:3}")
                .throwsBadSyntax("Macro .* is already defined");
    }

    @DisplayName("Loads classes from Jar file and the macros, those also, which are already loaded")
    @Test
    void testLoadAllMacros() throws Exception {
        final var test = TestThat.theInput("{@try! {@array :2:0:1:2:3}}{@maven:load (update)  com.javax0.jamal:jamal-test:1.12.7-SNAPSHOT}{@array :2:0:1:2:3}");
        final var processor = test.getProcessor();
        final var dateBefore = processor.getRegister().getMacro("date").get();
        test.results("There is no built-in macro with the id 'array'; did you mean '@try'?2");
        final var dateAfter = processor.getRegister().getMacro("date").get();
        Assertions.assertNotSame(dateBefore, dateAfter);
    }

}
