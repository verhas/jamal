package javax0.jamal.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestProcessor {

    @Test
    @DisplayName("Test that version parsing works for valid strings")
    void testVersionParsing() {
        for (int f = 1; f < 10; f++) {
            for (int i = 0; i < 10; i++) {
                for (int u = 0; u < 10; u++) {
                    for (int p = 0; p < 10; p++) {
                        for (final String pre : List.of("-SNAPSHOT", "")) {
                            final var versionString = f + "." + i + "." + u + "." + p + pre;
                            final var version = Processor.jamalVersion(versionString);
                            Assertions.assertEquals(f, version.feature());
                            Assertions.assertEquals(i, version.interim());
                            Assertions.assertEquals(u, version.update());
                            Assertions.assertEquals(p, version.patch());
                            Assertions.assertEquals(pre.replace("-", ""), version.pre().orElse(""));
                        }
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Test that there is a Jamal version, parsable and it is later than 1.6.3 (after which 'require' was introduced)")
    void testCurrentJamalVersion() {
        final var currentVersion = Processor.jamalVersion();
        final var latestReleaseWithoutRequire = Processor.jamalVersion("1.6.3");
        Assertions.assertTrue(currentVersion.compareTo(latestReleaseWithoutRequire) > 0);
    }
}
