package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Processor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

/**
 * Write the Jamal output to a file.
 * <p>
 * The caller can create the object specifying the character set and the writeable flag. The default character set is
 * UTF-8 and the default writeable flag is {@code false}. (Output is read only to prevent accidental editing.)
 */
public class OutputFile {

    private final Charset charset;
    private final boolean writeable;


    public OutputFile() {
        this(StandardCharsets.UTF_8, true);
    }

    public OutputFile(final Processor processor) {
        this(get(processor, "output:charset", Charset::forName, StandardCharsets.UTF_8),
                get(processor, "output:writable", Boolean::valueOf, true));
    }

    private static <T> T get(Processor processor, String id, Function<String, T> f, T defaulT) {
        try {
            final var s = MacroReader.macro(processor).readValue(id);
            if (s.isPresent()) {
                return f.apply(s.get());
            } else {
                return defaulT;
            }
        } catch (BadSyntax e) {
            return defaulT;
        }
    }

    public OutputFile(Charset charset, boolean writeable) {
        this.charset = charset;
        this.writeable = writeable;
    }

    public OutputFile(String charset, boolean writeable) {
        this.charset = Charset.forName(charset);
        this.writeable = writeable;
    }


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
    public void save(Path output, String result) throws IOException {
        final var parent = output.getParent();
        if (parent != null && !Files.exists(output.getParent())) {
            Files.createDirectories(output.getParent());
        }
        final var file = output.toFile();
        //noinspection ResultOfMethodCallIgnored
        file.setWritable(true);
        Files.write(output, result.getBytes(charset),
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE);
        //noinspection ResultOfMethodCallIgnored
        file.setWritable(writeable);
    }

}
