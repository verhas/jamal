package javax0.jamal.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Pattern;

public class TestEnvironmentNoDebug {
    @Test
    @DisplayName("The environment variable JAMAL_DEBUG is not set")
    void test() {
        Assertions.assertFalse(EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_DEBUG_ENV).isPresent());
    }

    private static final Pattern JSON_VERSION_LINE = Pattern.compile("^\\s*\"version\"\\s*:\\s*\"(.*?)\"\\s*,\\s*$");

    @Test
    @DisplayName("The ui has the same version as the other parts of the code")
    void testUiBackEndVersionIsTheSame() {
        Assertions.assertEquals(backEndVersion(), frontEndVersion(),
                "The back-end version and the front-end versions must be the same\n" +
                        "Execute the shell script ${projectRoot}/jamal-debug-ui/deployprod and git commit/push.");
    }

    @Test
    @DisplayName("EnvironmentVariables.getenv() reads the ~/.jamal/setting.properties file")
    void testProperties() throws Exception {
        final var jamalDirectory = System.getProperty("user.home") + "/.jamal/";
        final var jamalPFile = jamalDirectory + "settings.properties";
        Assumptions.assumeTrue(new File(jamalPFile).exists());
        final var EXPECTED = "Peter Verhas' macbook";
        Assertions.assertEquals(EXPECTED, EnvironmentVariables.getenv("jamal.testproperty").orElse(null),
                "When you compile Jamal if there is a ~/.jamal/settings.properties file it is read\n" +
                        "and the value of the property testproperty is used.\n" +
                        "It has to be \"" + EXPECTED + "\"\n" +
                        "You should delete the properties file, use settings.xml or set the property,\n" +
                        "or disable this test,\n"+
                        "or do not compile Jamal.");
    }

    private static String backEndVersion() {
        final var version = new Properties();
        Processor.jamalVersion(version);
        return version.getProperty("version");
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
