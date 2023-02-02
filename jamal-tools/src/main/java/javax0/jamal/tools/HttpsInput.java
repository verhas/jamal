package javax0.jamal.tools;

import javax0.jamal.api.ResourceReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class HttpsInput implements ResourceReader {
    private static final String RESOURCE_PREFIX = "https:";
    private static final int RESOURCE_PREFIX_LENGTH = RESOURCE_PREFIX.length();

    @Override
    public boolean canRead(final String fileName) {
        return fileName.startsWith(RESOURCE_PREFIX + "//");
    }

    @Override
    public int fileStart(final String fileName) {
        return RESOURCE_PREFIX_LENGTH + 2;
    }


    @Override
    public String read(String fileName) throws IOException {
        return read(fileName,true);
    }

    /**
     * Read the content of the resource as a UTF-8 encoded character stream
     *
     * @param fileName the name of the resource (already without the {@code res:} prefix
     * @return the content of the resource as a string
     * @throws IOException if the resource cannot be read
     */
    @Override
    public String read(String fileName, boolean noCache) throws IOException {
        return CachedHttpInput.getInput(fileName,noCache).toString();
    }
}
