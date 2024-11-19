package javax0.jamal.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * A utility class that loads environment variables from a <code>.env</code> file into system properties.
 *
 * <p>This class reads a <code>.env</code> file from the current working directory and loads key-value pairs
 * into the system properties. The <code>.env</code> file should contain lines in the format <code>KEY=VALUE</code>.
 * Lines starting with <code>#</code> or empty lines are ignored.</p>
 */
public class DotEnvLoder {
    private static final String ENV_FILE = ".env";
    private static boolean isLoaded = false;

    /**
     * Loads environment variables from the <code>.env</code> file into system properties.
     *
     * <p>This method reads the <code>.env</code> file located in the current working directory and loads the
     * environment variables defined into the system properties. It ensures that the file is loaded
     * only once even if this method is called multiple times.</p>
     *
     * @throws IOException if an I/O error occurs reading the file
     */
    public static void load() throws IOException {
        if (isLoaded) {
            return;  // Prevent multiple loads
        }

        Path currentPath = Paths.get("").toAbsolutePath();
        Path envPath = currentPath.resolve(ENV_FILE);

        if (!envPath.toFile().exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final var trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                Optional.of(trimmed.indexOf('='))
                        .filter(index -> index > 0)
                        .ifPresent(index -> {
                            String key = trimmed.substring(0, index).trim();
                            String value = parseValue(trimmed.substring(index + 1));
                            System.setProperty(key, value);
                        });
            }
        }

        isLoaded = true;
    }

    /**
     * Parses a value from the <code>.env</code> file, handling optional surrounding quotes.
     *
     * @param value the raw value string from the <code>.env</code> file
     * @return the parsed value with any surrounding quotes removed and trimmed whitespace
     */
    private static String parseValue(String value) {
        if (value.length() >= 2) {
            char firstChar = value.charAt(0);
            char lastChar = value.charAt(value.length() - 1);
            if ((firstChar == '"' && lastChar == '"') || (firstChar == '\'' && lastChar == '\'')) {
                return value.substring(1, value.length() - 1);
            }else {
                return value.trim();
            }
        }
        // if it is a single space then return empty string, otherwise return the one character
        return value.trim();
    }

}
