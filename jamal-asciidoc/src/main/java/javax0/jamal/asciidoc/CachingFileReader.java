package javax0.jamal.asciidoc;

import javax0.jamal.api.Processor;

import java.util.HashMap;
import java.util.Map;

public class CachingFileReader implements Processor.FileReader {
    final boolean off;
    final Map<String, String> readFiles = new HashMap<>();

    public CachingFileReader(final boolean off) {
        this.off = off;
    }

    String list() {
        final var sb = new StringBuilder();
        for (final var e : readFiles.entrySet()) {
            sb.append("  ").append(e.getKey()).append(" ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * This reader calculates the MD5 checksum of the files read and stores it in a map. Later the cache will get this
     * (fileName,md5) map and uses it to check if there was any included, imported or any other way used file changed
     * in the meantime.
     * <p>
     * This is suboptimal because some macros, like snippet handling uses only a part of the file and in case
     * other parts changed the result is still a new processing, but that cannot be avoided, because knowing
     * what part is needed already needs the processing.
     *
     * @param fileName the name of the file, which was passed to the {@link #read(String)} method (not the altered
     *                 name returned).
     * @param content  the content of the file, which was read by the processor.
     */
    @Override
    public void set(final String fileName, final String content) {
        if (!off) {
            readFiles.put(fileName, Md5Calculator.md5(content));
        }
    }

    @Override
    public Processor.IOHookResult read(final String fileName) {
        return Processor.IOHookResult.IGNORE;
    }
}
