package javax0.jamal.asciidoc.converters;

import javax0.jamal.asciidoc.TextConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Xml conversion could be just left for the text converter, but this converter also recognizes pom.jam as xml.
 */
public class XmlConverter extends AbstractConverter {
    @Override
    public List<String> convert(final List<String> original) {
        final var sourcedLines = new ArrayList<String>();
            sourcedLines.add("[source,xml]");
        return TextConverter.convertText(original, sourcedLines);
    }

    @Override
    public boolean canConvert(final String fileName) {
        return super.canConvert(fileName) || new File(fileName).getName().equals("pom.jam");
    }

    private static final List<String> ex = List.of("xml");

    @Override
    List<String> extensions() {
        return ex;
    }
}
