package javax0.jamal.tools;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.BindException;
import java.nio.charset.StandardCharsets;

class ResourceInput {
    /**
     * Read the content of the resource as a UTF-8 encoded character stream
     *
     * @param fileName the name of the resource (already without the {@code res:} prefix
     * @return the content of the resource as a string
     * @throws IOException if the resource cannot be read
     */
    static String getInput(String fileName) throws IOException {
        try (final var is = FileTools.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new BindException("The resource file 'res:" + fileName + "' cannot be read.");
            }
            final var reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            final var writer = new StringWriter();
            reader.transferTo(writer);
            return writer.toString();
        }
    }
}
