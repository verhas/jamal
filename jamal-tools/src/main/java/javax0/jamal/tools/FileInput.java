package javax0.jamal.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileInput {

    /**
     * Get the content of a file.
     *
     * @param fileName is the name of the file.
     * @return the content of the file
     * @throws IOException if the file cannot be read
     */
    static String getInput(String fileName) throws IOException {
        return Files.readString(Paths.get(fileName));
    }
}
