package javax0.jamal.asciidoc;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax0.jamal.asciidoc.Md5Calculator.md5;
import static javax0.jamal.tools.FileTools.getFileContent;

/**
 * A instance of this class holds the cached value of the last run.
 */
class ProcessingCache {
    final String md5;
    final List<String> newLines;
    final Map<String, String> readFiles = new HashMap<>();

    boolean isTheSame(String md5) {
        if (md5.equals(this.md5)) {
            final var p = new Processor();
            for (final var f : readFiles.keySet()) {
                try {
                    if (!readFiles.get(f).equals(md5(getFileContent(f, p)))) {
                        return false;
                    }
                } catch (BadSyntax e) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    ProcessingCache(final String md5, final List<String> newLines, CachingFileReader reader) {
        this.md5 = md5;
        this.newLines = newLines;
        if (reader != null) {
            this.readFiles.putAll(reader.readFiles);
        }
    }
}
