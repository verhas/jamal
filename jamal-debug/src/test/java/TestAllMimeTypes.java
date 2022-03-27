import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class TestAllMimeTypes {

    /**
     * Check that all the mime types are present in the file {@code mime-types.properties}, which are needed to deliver
     * the static JS/CSS/HTML code to the browser.
     *
     * @throws URISyntaxException in case of some internal error
     * @throws IOException        in case of some internal error
     */
    @Test
    @DisplayName("There is a mime type in mime-types.properties for each extension")
    void testMimeTypeMissing() throws URISyntaxException, IOException {
        Properties mimeTypes = new Properties();
        mimeTypes.load(TestAllMimeTypes.class.getClassLoader().getResourceAsStream("mime-types.properties"));
        final var uiDirectory = Paths.get(
            requireNonNull(TestAllMimeTypes.class.getClassLoader().getResource("ui")).toURI()
        );
        Files.walk(uiDirectory, Integer.MAX_VALUE)
            .filter(Files::isRegularFile)
            .map(Path::toString)
            .filter(s -> s.lastIndexOf('.') != -1)
            .map(s -> s.substring(s.lastIndexOf('.') + 1))
            .filter(s -> mimeTypes.getProperty(s) == null)
            .findAny()
            .ifPresent(s -> Assertions.fail("There is no mime type for the file extension '" + s + "'"));
    }

    @Test
    @DisplayName("There is no unused mime type in mime-types.properties for each extension")
    void testMimeTypeSurplus() throws URISyntaxException, IOException {
        Properties mimeTypes = new Properties();
        mimeTypes.load(TestAllMimeTypes.class.getClassLoader().getResourceAsStream("mime-types.properties"));
        final var extensions = new HashSet<>(mimeTypes.stringPropertyNames());
        final var uiDirectory = Paths.get(
            requireNonNull(TestAllMimeTypes.class.getClassLoader().getResource("ui")).toURI()
        );
        Files.walk(uiDirectory, Integer.MAX_VALUE)
            .filter(Files::isRegularFile)
            .map(Path::toString)
            .filter(s -> s.lastIndexOf('.') != -1)
            .map(s -> s.substring(s.lastIndexOf('.') + 1))
            .forEach(extensions::remove);
        if (!extensions.isEmpty()) {
            Assertions.fail("There are extra mime types not used:\n" + String.join(",", extensions));
        }
    }
}
