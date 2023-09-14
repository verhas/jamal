package javax0.jamal.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OutputFile {
    /**
     * Write the result to the output file.
     * <p>
     * The method writes the content of the {@code result} string to the {@code output} file. The method creates the
     * parent directories if they do not exist. The method sets the file to read/write before writing and sets it back
     * to read only after writing. This is to avoid accidental modification of the file.
     *
     * @param output the file to write the result to
     * @param result the result to write
     * @throws IOException if the file cannot be written
     */
    public static void save(Path output, String result) throws IOException {
        final var parent = output.getParent();
        if (parent != null && !Files.exists(output.getParent())) {
            Files.createDirectories(output.getParent());
        }
        final var file = output.toFile();
        //noinspection ResultOfMethodCallIgnored
        file.setWritable(true);
        Files.write(output, result.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE);
        //noinspection ResultOfMethodCallIgnored
        file.setWritable(false);
    }

}
