package javax0.jamal.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class FileInput {

    /**
     * Get the content of a file.
     *
     * @param fileName is the name of the file.
     * @return the content of the file
     * @throws IOException if the file cannot be read
     */
    static String getInput(String fileName) throws IOException {
        if (Objects.equals(System.lineSeparator(), "\n")) {
            return Files.readString(Paths.get(fileName), StandardCharsets.UTF_8);
        } else {
            return Files.readString(Paths.get(fileName), StandardCharsets.UTF_8)
                    .chars()
                    .filter(c -> c != '\r')
                    .mapToObj(c -> (char) c)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
        }
    }
}
