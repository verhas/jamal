package javax0.jamal.asciidoc;

import javax0.jamal.api.ServiceLoaded;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Converters implement this interface so that the service loader can load them.
 * <p>
 * The plugin can work not only with Jamal extended Asciidoc documents, but also with any Jamal extended document,
 * which can be converted to Asciidoc. For example text documents are converted so that they will appear in the
 * document verbatim. Markdown documents are converted to asciidoc for the display.
 * <p>
 * At the same time the saved file, the plugin generates chopping off the '.jam' file extension is NOT converted.
 */
public interface Converter {

    /**
     * Convert the original format to asciidoc so that the document will be displayed properly.
     *
     * @param original the text in original format
     * @return the converted document
     */
    List<String> convert(List<String> original);

    /**
     * Should return {@code true} if it can handle the file and can convert a file to asciidoc.
     * The full file name is given, though the decision is usually done based on the file extension.
     *
     * @param fileName the name of the input file that is to be converted.
     * @return {@code true} if the implementation is ready to convert the file to asciidoc
     */
    boolean canConvert(final String fileName);

    static List<Converter> getInstances() {
        ServiceLoader<Converter> services = ServiceLoader.load(Converter.class);
        final var list = new ArrayList<Converter>();
        services.iterator().forEachRemaining(list::add);
        if (list.size() == 0) {
            ServiceLoaded.loadViaMetaInf(Converter.class, list);
        }
        return list;
    }
}
