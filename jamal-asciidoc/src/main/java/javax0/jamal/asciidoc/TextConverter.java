package javax0.jamal.asciidoc;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a "default" converter that converts TEXT files. This is not an implementation of the {@link Converter}
 * interface.
 */
public class TextConverter {
    public static List<String> convert(final String fileName, final List<String> original) {
        final var outputFileName = fileName.substring(0, fileName.length() - ".jam".length());
        final var innerExt = outputFileName.lastIndexOf('.');
        final String type;
        if (innerExt == -1) {
            type = "";
        } else {
            type = outputFileName.substring(innerExt + 1);
        }
        final var sourcedLines = new ArrayList<String>();
        if (type.length() > 0) {
            sourcedLines.add("[source," + type + "]");
        } else {
            sourcedLines.add("[source]");
        }
        return convertText(original, sourcedLines);
    }

    public static List<String> convertText(final List<String> original, final ArrayList<String> sourcedLines) {
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
