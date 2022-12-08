package javax0.jamal.asciidoc;

import javax0.jamal.api.Processor;

import java.util.HashMap;
import java.util.Map;

public class CachingFileReader implements Processor.FileReader {

    /**
     * {@code true} when we do not need to calculate and store the md5 values of the files, because only the main
     * file change is cared about the caching. It is assumed that the external files do not change or their processing
     * is too expensive and the user uses the {@code noDependencies} option at the start of the Jamal file. If the user
     * really wants an update when a dependent file has changed all it has to do it insert a space into the main file
     * they are editing and then delete it.
     */
    final boolean off;
    /**
     * The (file_name, md5) pairs of the dependent files.
     */
    final Map<String, String> files = new HashMap<>();

    public CachingFileReader(final boolean off) {
        this.off = off;
    }

    /**
     * Get the list of the files this reader was reading so far. This method is used for log purposes.
     *
     * @return the string containing the list of the file names space separated and a new line.
     */
    String list() {
        final var sb = new StringBuilder();
        for (final var e : files.entrySet()) {
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
            files.put(fileName, Md5Calculator.md5(content));
        }
    }

    /**
     * @param fileName the original name of the file
     * @return {@link Processor.IOHookResult#IGNORE} signalling, that we did not read the file, the next on the hook
     * can read it.
     */
    @Override
    public Processor.IOHookResult read(final String fileName) {
        return Processor.IOHookResult.IGNORE;
    }
}
