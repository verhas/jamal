package javax0.jamal.test.core;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestEnv {

    @Test
    @DisplayName("test that env returns the ... whatever it returns. we assume there is a JAVA_HOME and there is no CICADA_HOME")
    void testEnv() throws Exception {
        final var javaHome = System.getenv("JAVA_HOME");
        final var cicaHome = System.getenv("CICA_HOME");
        Assertions.assertNull(cicaHome);

        TestThat.theInput(
            "{@env JAVA_HOME}\n" +
                "JAVA_HOME {#if /{@env JAVA_HOME ?}/is defined/is not defined}\n" +
                "{@env CICA_HOME}\n" +
                "CICA_HOME {#if /{@env CICA_HOME ?}/is defined/is not defined}\n"
        ).results(
            javaHome + "\n" +
                "JAVA_HOME is defined\n" +
                "\n" +
                "CICA_HOME is not defined\n"
        );
    }
}
