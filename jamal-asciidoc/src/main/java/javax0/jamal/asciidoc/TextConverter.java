package javax0.jamal.asciidoc;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a "default" converter that converts TEXT files. This is not an implementation of the {@link Converter}
 * interface.
 */
public class TextConverter {
    public static List<String> convert(final List<String> original) {
        final var sourcedLines = new ArrayList<String>();
        sourcedLines.add("[source]");
        sourcedLines.add("----");
        for (final var line : original) {
            // add an invisible space that will fool asciidoctor not to end the source block
            if (line.trim().equals("----")) {
                sourcedLines.add(line.replaceAll("----", "----\u200F\u200F\u200E \u200E"));
            } else {
                sourcedLines.add(line);
            }
        }
        sourcedLines.add("----");
        return sourcedLines;
    }
}
