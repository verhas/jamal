package javax0.jamal.tools;

import javax0.jamal.api.Processor;
import javax0.jamal.api.ResourceReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class ResourceInput implements ResourceReader {
    private static final String RESOURCE_PREFIX = "res:";
    private static final int RESOURCE_PREFIX_LENGTH = RESOURCE_PREFIX.length();

    private Processor processor;

    @Override
    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    @Override
    public boolean canRead(final String fileName) {
        return fileName.startsWith(RESOURCE_PREFIX);
    }

    @Override
    public int fileStart(final String fileName) {
        return RESOURCE_PREFIX_LENGTH;
    }

    /**
     * Read the content of the resource as a UTF-8 encoded character stream
     *
     * @param fileName the name of the resource (already without the {@code res:} prefix
     * @return the content of the resource as a string
     * @throws IOException if the resource cannot be read
     */
    @Override
    public String read(final String fileName) throws IOException {
        String fn = fileName.substring(RESOURCE_PREFIX_LENGTH);
        final ClassLoader classLoader;
        if (fn.charAt(0) == '`') {
            final var index = fn.indexOf('`', 1);
            if (index == -1) {
                throw new IOException("The resource name macro reference is not properly quoted with backticks");
            }
            final var macroName = fn.substring(1, index);
            fn = fn.substring(index + 1);
            final var macro = processor.getRegister().getMacro(macroName)
                    .orElseThrow(() -> new IOException(String.format("The macro '%s' in the resource '%s' is not defined", macroName, fileName)));
            classLoader = macro.getClass().getClassLoader();
        } else {
            classLoader = FileTools.class.getClassLoader();
        }
        try (final var is = classLoader.getResourceAsStream(fn)) {
            if (is == null) {
                throw new IOException("The resource file '" + fileName + "' cannot be read.");
            }
            try (final var writer = new StringWriter()) {
                new InputStreamReader(is, StandardCharsets.UTF_8).transferTo(writer);
                return writer.toString();
            }
        }
    }
}
