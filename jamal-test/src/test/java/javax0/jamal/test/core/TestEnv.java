package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test the {@code env} macro. Note that this test cannot be implemented using YAML test. It can but then the env macro
 * would just be tested using itself.
 */
public class TestEnv {

    @Test
    @DisplayName("test that env returns the ... whatever it returns. we assume there is a JAVA_HOME and there is no CICA_HOME")
    void testEnv() throws Exception {
        final var javaHome = System.getenv("JAVA_HOME");
        final var cicaHome = System.getenv("CICA_HOME");
        Assertions.assertNull(cicaHome);

        TestThat.theInput(
            "{@env JAVA_HOME}\n" +
                "{@env JAVA_HOME !}\n" +
                "JAVA_HOME {#if /{@env JAVA_HOME ?}/is defined/is not defined}\n" +
                "{@env CICA_HOME}\n" +
                "CICA_HOME {#if /{@env CICA_HOME ?}/is defined/is not defined}\n"
        ).results(
            javaHome + "\n" +
                javaHome + "\n" +
                "JAVA_HOME is defined\n" +
                "\n" +
                "CICA_HOME is not defined\n"
        );
    }

    @Test
    @DisplayName("test that env throws up when the variable is not defined and we use a ! after the name CICADA_HOME")
    void testEnvThrow() throws Exception {
        final var cicaHome = System.getenv("CICADA_HOME");
        Assertions.assertNull(cicaHome);

        TestThat.theInput(
            "{@env CICADA_HOME !}\n"
        ).throwsBadSyntax("Environment variable 'CICADA_HOME' is not defined.*");
    }
}
