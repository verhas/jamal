package javax0.jamal.asciidoc.converters;

import javax0.jamal.asciidoc.converters.AbstractConverter;

import java.util.Arrays;
import java.util.List;

import static nl.jworks.markdown_to_asciidoc.Converter.convertMarkdownToAsciiDoc;

public class MarkdownConverter extends AbstractConverter {
    private static final List<String> ex = List.of("md", "markdown");

    @Override
    List<String> extensions() {
        return ex;
    }

    @Override
    public List<String> convert(final List<String> original) {
        return Arrays.asList(convertMarkdownToAsciiDoc(String.join("\n", original)).split("\n"));
    }
}
