package javax0.jamal.maven.load;

import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * Test that the macro {@code maven:load} can load classes from a Jar file and the macros from the Jar file.
 * <p>
 * Every test in this file evaluates a Jamal macro text.
 * This text first ensures that the macro {@code array} is not loaded yet.
 * Then it executes the macro {@code maven:load} loading the classes from the {@code jamal-test} JAR file and
 * tests that the {@code array} macro, this time, is loaded.
 */
public class TestLoadMavenJar {
    public static final String TEST_MACRO = "{@array :2:0:1:2:3}"; // macro implemented in the jamal-test JAR file
    private static final String TEST_STRING = String.format(""
            + "{@try! %s}"
            + "{@maven:load %%s com.javax0.jamal:jamal-test:%s}"
            + "%s",
            TEST_MACRO, getVersionString(), TEST_MACRO);
    private static final String RESULT = "There is no built-in macro with the id 'array'; did you mean '@try'?2";
    public static final String CORE_MACRO = "define"; // to check if it was reloaded

    private static String getVersionString() {
        final var version = new Properties();
        Processor.jamalVersion(version);
        return version.getProperty("version");
    }

    /**
     * This tests does not use any option for the macro loading, therefore it will load only the macros which are not loaded yet.
     * It does not matter that the core macro {@code define} is on the classpath again, because it is already loaded and
     * this time ignored. Therefore, the instance in the register is the same before and after the processing.
     *
     * @throws Exception never, hopefully
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @DisplayName("Loads classes from Jar file but not the macros, which are already loaded")
    @Test
    void testLoadOnlyNewMacros() throws Exception {
        final var test = TestThat.theInput(String.format(TEST_STRING, ""));
        final var processor = test.getProcessor();
        final var coreMacroBefore = processor.getRegister().getMacro(CORE_MACRO).get();
        test.results(RESULT);
        final var coreMacroAfter = processor.getRegister().getMacro(CORE_MACRO).get();
        Assertions.assertSame(coreMacroBefore, coreMacroAfter);
    }

    /**
     * This test uses the 'update' option telling the loader to overload all the macros that it finds.
     * The macro {@code define} is defined in {@code jamal-core}, which is a dependency of {@code jamal-test}.
     * This is loaded because of the 'update' option. Therefore, the macro {@code define} is reloaded and
     * the instances are not the same before and after the processing.
     *
     * @throws Exception never, hopefully
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @DisplayName("Loads classes from Jar file and the macros, those also, which are already loaded")
    @Test
    void testLoadAllMacros() throws Exception {
        final var test = TestThat.theInput(String.format(TEST_STRING, "(update)"));
        final var processor = test.getProcessor();
        final var coreMacroBefore = processor.getRegister().getMacro(CORE_MACRO).get();
        test.results(RESULT);
        final var coreMacroAfter = processor.getRegister().getMacro(CORE_MACRO).get();
        Assertions.assertNotSame(coreMacroBefore, coreMacroAfter);
    }


    /**
     * This test uses the 'update' option telling the loader to overload all the macros that it finds.
     * However, it also uses the 'noDep' option telling the loader to not load the dependencies.
     * The macro {@code define} is defined in {@code jamal-core}, which is a dependency of {@code jamal-test}.
     * This time, however, is not loaded because of the 'noDep' option. Therefore, the macro {@code define} is not
     * reloaded and the instance before and after the execution is the same.
     *
     * @throws Exception never, hopefully
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @DisplayName("Loads classes from Jar file and the macros, but not from the dependencies")
    @Test
    void testLoadNoDepsAllMacros() throws Exception {
        final var test = TestThat.theInput(String.format(TEST_STRING, "(update noDeps)"));
        final var processor = test.getProcessor();
        final var coreMacroBefore = processor.getRegister().getMacro(CORE_MACRO).get();
        test.results(RESULT);
        final var coreMacroAfter = processor.getRegister().getMacro(CORE_MACRO).get();
        Assertions.assertSame(coreMacroBefore, coreMacroAfter);
    }
}
