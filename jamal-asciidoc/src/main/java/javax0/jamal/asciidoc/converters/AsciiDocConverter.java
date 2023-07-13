package javax0.jamal.asciidoc.converters;

import java.util.List;

/**
 * The simplest ever converter.
 * It just returns the original list of lines.
 */
public class AsciiDocConverter extends AbstractConverter {
    @Override
    public List<String> convert(final List<String> original) {
        return original;
    }

    private static final List<String> ex = List.of("adoc", "ad", "asciidoc");

    @Override
    List<String> extensions() {
        return ex;
    }

}
