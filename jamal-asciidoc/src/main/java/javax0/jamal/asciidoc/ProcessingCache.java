package javax0.jamal.asciidoc;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax0.jamal.asciidoc.Md5Calculator.md5;
import static javax0.jamal.tools.FileTools.getFileContent;

/**
 * An instance of this class holds the cached value of the last run.
 * <p>
 * The object holds the md5 of the file content, the file content in a string list and the names of the dependent files
 * and all md5 values of those file contents.
 */
class ProcessingCache {

    /**
     * The md5 print of the content of the main file this cache item stores
     */
    final String md5;

    /**
     * The file content of the file this cache item stores
     */
    final List<String> lines;

    /**
     * The (file_name, md5) pairs of the dependent files.
     */
    final Map<String, String> files = new HashMap<>();

    /**
     * This method checks if the file in the cache is stale or not.
     * <p>
     * To decide it checks the provided md5 value against the md5 value of the file content stored in the cache,
     * The main file assumed unchanged if these values are the same.
     * <p>
     * After that the method reads all the files this file depends on, calculates their md5 values and compares each
     * with the one stored. If there is a difference in any of them of if any of the files are not readable then the
     * cache is stale.
     * <p>
     * If all the md5 values match the cache item can be used.
     *
     * @param md5 the md5 value of the file content as it is now, to be compared with the old one
     * @return {@code true} if all the comparisons show that the file and the dependent files did not change.
     * {@code false} if the cache item is stale.
     */
    boolean isTheSame(final String md5) {
        if( md5 ==  null ){
            // if we could not calculate the md5, because there is no such algorithm then we just ignore the cache
            return false;
        }
        if (md5.equals(this.md5)) {
            final var p = new Processor();
            for (final var e : files.entrySet()) {
                try {
                    if (!e.getValue().equals(md5(getFileContent(e.getKey(), p)))) {
                        return false;
                    }
                } catch (BadSyntax _ignored) {
                    // happens when a file cannot be read
                    // in that case the file definitely hs changed and it is a same assumption
                    // that this cache item is stale
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create a new cache item. The item will contains the content of a text file as well as all the tex file contents
     * this file depends on.
     * <p>
     * This item does not keep a reference to the caching reader. It simple reads the list of the dependent files from
     * it and stores those in a list.
     *
     * @param md5    the md5 value of the file content
     * @param lines  the lines of the file
     * @param reader the reader that was used to read this file, able to list all the dependencies
     */
    ProcessingCache(final String md5, final List<String> lines, final CachingFileReader reader) {
        this.md5 = md5;
        this.lines = lines;
        if (reader != null) {
            this.files.putAll(reader.files);
        }
    }
}
