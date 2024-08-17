package javax0.jamal.api;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class DotEnvLoder {
    private static final String ENV_FILE = ".env";
    private static boolean isLoaded = false;

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
