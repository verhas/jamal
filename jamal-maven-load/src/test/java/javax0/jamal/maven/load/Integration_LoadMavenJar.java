package javax0.jamal.maven.load;

import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Properties;
import java.util.function.Supplier;

/**
 * Test that the macro {@code maven:load} can load classes from a Jar file and the macros from the Jar file.
 * <p>
 * Every test in this file evaluates a Jamal macro text.
 * This text first ensures that the macro {@code array} is not loaded yet.
 * Then it executes the macro {@code maven:load} loading the classes from the {@code jamal-test} JAR file and
 * tests that the {@code array} macro, this time, is loaded.
 * <p>
 * The tests also check the security mocking the configuration. The configuration is injected through a package private
 * field.
 */
public class Integration_LoadMavenJar {
    /**
     * The test string contains the macro executed by the different tests.
     * The first macro use will result an error string to demonstrate that the macro 'array' was not loaded.
     * After that the 'maven:load' loads the macro from the actual version of jamal-test.
     * In the last line the test macro is called again, this time without the 'try' surrounding.
     * It will result 2.
     */
    private static final String TEST_STRING = String.format(""
            + "{@try! {@array}}"
            + "{@maven:load %%s com.javax0.jamal:jamal-test:%s}" // %%s -> options
            + "{@array :2:0:1:2:3}", Processor.jamalVersionString());
    private static final String RESULT = "There is no built-in macro with the id 'array'; did you mean '@try'?2";
    /**
     * We selected one core macro arbitrarily. The package test has the core as dependency. If the option 'update'
     * is used in the macro 'maven:load' then the core macros are also reloaded and updated. Otherwise, not.
     */
    public static final String CORE_MACRO = "define"; // to check if it was reloaded

    private static void addProperty(Properties props, String key, String value) {
        if (props.containsKey(key)) {
            return;
        }
        props.put(key, value);
    }

    private void setupSecurityMock(String... extraProperties) {
        final var props = setupEmptySecurityMock(extraProperties);
        addProperty(props, "maven.load.repo", "https://repo1.maven.org/maven2/");
        addProperty(props, "maven.load.local", ".target/myRepo");
        addProperty(props, "maven.load.include", "com.javax0.jamal:jamal-test:*");
        addProperty(props, "maven.load.exclude", ".com.javax0.jamal:jamal-api:*");

    }

    /**
     * Mock the configuration so that the tests can run without the need to have a configuration file.
     * <p>
     * A basic set of properties are added to the configuration and the argument can override any of these or add new.
     *
     * @param extraProperties the overriding or new properties, arguments are used in pars, have to be even number of
     *                        argument, or else index out of bounds exception will be thrown.
     */
    private Properties setupEmptySecurityMock(String... extraProperties) {
        final var props = new Properties();

        for (int i = 0; i < extraProperties.length; i += 2) {
            props.put(extraProperties[i], extraProperties[i + 1]);
        }

        //noinspection unchecked
        final var mock = (Supplier<Properties>) Mockito.mock(Supplier.class);
        Mockito.when(mock.get()).thenReturn(props);
        LoadMavenJar.configuration = mock;
        return props;
    }

    /**
     * This tests does not use any option for the macro loading, therefore it will load only the macros which are not
     * loaded yet.
     * <p>
     * It does not matter that the core macro {@code define} ({@code CORE_MACRO} is on the classpath again, because it
     * is already loaded and this time ignored. Therefore, the instance in the register is the same before and after the
     * processing.
     * <p>
     * The test also checks that the configuration {@code include} can contain a local path part.
     *
     * @throws Exception never, hopefully
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @DisplayName("Loads classes from Jar file but not the macros, which are already loaded")
    @Test
    void testLoadOnlyNewMacros() throws Exception {
        setupSecurityMock("maven.load.include", "com.javax0.jamal:jamal-test:*:myFile");
        final var test = TestThat.theInput(String.format(TEST_STRING, ""));
        test.atPosition(new Position("myFile", 1, 1));
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
        setupSecurityMock();
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
        setupSecurityMock();
        final var test = TestThat.theInput(String.format(TEST_STRING, "(update noDeps)"));
        final var processor = test.getProcessor();
        final var coreMacroBefore = processor.getRegister().getMacro(CORE_MACRO).get();
        test.results(RESULT);
        final var coreMacroAfter = processor.getRegister().getMacro(CORE_MACRO).get();
        Assertions.assertSame(coreMacroBefore, coreMacroAfter);
    }


    @DisplayName("Loads classes when the 'include' configuration refers to the top level file")
    @Test
    void worksWhenTopLevelFileNameMatch() throws Exception {
        setupSecurityMock(
                "maven.load.include", "com.javax0.jamal:jamal-test:*:myFile",
                "maven.load.exclude", "com.javax0.jamal:jamal-test:*:yourSomething"
        );
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("yourFile", 1, 1, new Position("myFile", 1, 1)))
                .results(RESULT);
    }

    @DisplayName("Fails when some file in the position stack is excluded")
    @Test
    void failForSubLevelMatch() throws Exception {
        setupSecurityMock(
                "maven.load.include", "com.javax0.jamal:jamal-test:*:myFile",
                "maven.load.exclude", "com.javax0.jamal:jamal-test:*:yourFile"
        );
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("anyFile", 1, 1, new Position("yourFile", 1, 1, new Position("myFile", 1, 1))))
                .throwsUp(IllegalStateException.class, "The maven artifact 'com.javax0.jamal:jamal-test:1.12.7-SNAPSHOT' is excluded.");
    }

    @DisplayName("Fails when security includes the coordinates, but not for this file")
    @Test
    void failWhenFileNameDoesNotMatch() throws Exception {
        setupSecurityMock("maven.load.include", "com.javax0.jamal:jamal-test:*:myFile");
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("yourFile", 1, 1))
                .throwsUp(IllegalStateException.class, "The maven artifact 'com.javax0.jamal:jamal-test:.*' is not included.");
    }

    @DisplayName("Fails when security includes the coordinates, but the group id does not match")
    @Test
    void failWhenGroupNameDoesNotMatch() throws Exception {
        setupSecurityMock("maven.load.include", "org.javax0.jamal:jamal-test:*:myFile"); // <--- org instead of com
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("myFile", 1, 1))
                .throwsUp(IllegalStateException.class, "The maven artifact 'com.javax0.jamal:jamal-test:.*' is not included.");
    }

    @DisplayName("Fails when security includes the coordinates, but the artifact id does not match")
    @Test
    void failWhenArtifactNameDoesNotMatch() throws Exception {
        setupSecurityMock("maven.load.include", "com.javax0.jamal:jamal-tost:*:myFile"); // <--- tost instead of test
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("myFile", 1, 1))
                .throwsUp(IllegalStateException.class, "The maven artifact 'com.javax0.jamal:jamal-test:.*' is not included.");
    }

    @DisplayName("Fails when security includes the coordinates, but the version does not match")
    @Test
    void failWhenVersionNameDoesNotMatch() throws Exception {
        setupSecurityMock("maven.load.include", "com.javax0.jamal:jamal-test:1.2023.1:myFile"); // <--- tost instead of test
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("myFile", 1, 1))
                .throwsUp(IllegalStateException.class, "The maven artifact 'com.javax0.jamal:jamal-test:.*' is not included.");
    }

    @DisplayName("Fails when there is no maven.load.include configured")
    @Test
    void failEmptyConfiguration() throws Exception {
        setupEmptySecurityMock();
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("myFile", 1, 1))
                .throwsUp(IllegalStateException.class, "There is no 'maven.load.include' property in the configuration.*");
    }

    @DisplayName("Fails when there is no maven.load.include configured")
    @Test
    void failExcluded() throws Exception {
        setupEmptySecurityMock(
                "maven.load.include", "com.javax0.jamal:jamal-test:*", // we are allowed to download but
                "maven.load.exclude", "com.javax0.jamal:jamal-test:*:burka/murka/" // not from this directory
        );
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("burka/murka/myFile", 1, 1))
                .throwsUp(IllegalStateException.class, "The maven artifact 'com.javax0.jamal:jamal-test:.*' is excluded.");
    }

    @DisplayName("Fails when artifactId is wildcard, but version is not")
    @Test
    void failtInconsistentWildCardUse() throws Exception {
        setupSecurityMock("maven.load.include", "com.javax0.jamal:*:1.13.7");
        TestThat.theInput(String.format(TEST_STRING, ""))
                .atPosition(new Position("burka/murka/myFile", 1, 1))
                .throwsUp(IllegalArgumentException.class, "The the artifact id must not be '\\*' if the version is not '\\*'");
    }
}
