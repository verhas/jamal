package javax0.jamal.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

public class TestEnvironmentNoDebug {
    @Test
    @DisplayName("The environment variable JAMAL_DEBUG is not set")
    void test() {
        Assertions.assertNull(
            Optional.ofNullable(System.getenv(Debugger.JAMAL_DEBUG_ENV)).orElseGet(
                () -> System.getProperty(Debugger.JAMAL_DEBUG_SYS)
            ));
    }

    private static final Pattern JSON_VERSION_LINE = Pattern.compile("^\\s*\"version\"\\s*:\\s*\"(.*?)\"\\s*,\\s*$");

    @Test
    @DisplayName("The ui has the same version as the other parts of the code")
    void testUiBackEndVersionIsTheSame() {
        Assertions.assertEquals(backEndVersion(),frontEndVersion(),
            "The back-end version and the front-end versions must be the same\n" +
                "Execute the shell script ${projectRoor}/jamal-debug-ui/deploybuild and git commit/push.");
    }

    private static String backEndVersion() {
        final var version = new Properties();
        Processor.jamalVersion(version);
        final var beVersionString = version.getProperty("version");
        return beVersionString;
    }

    private static String frontEndVersion() {
        try (final var is = new FileInputStream("../jamal-debug-ui/package.json");
             final var isr = new InputStreamReader(is);
             final var reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                final var matcher = JSON_VERSION_LINE.matcher(line);
                if (matcher.matches()) {
                    return matcher.group(1);
                }
            }
        } catch (FileNotFoundException e) {
            Assertions.fail("DEBUG UI package.json does not exist", e);
        } catch (IOException ioException) {
            Assertions.fail("DEBUG UI package.json ioException", ioException);
        }
        return null;
    }
}
