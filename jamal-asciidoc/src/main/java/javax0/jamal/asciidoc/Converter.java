package javax0.jamal.asciidoc;

import javax0.jamal.api.ServiceLoaded;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Converters implement this interface so that the service loader can load them.
 * <p>
 * The plugin can work not only with Jamal extended AsciiDoc documents but also with any Jamal extended document,
 * which can be converted to AsciiDoc. For example, text documents are converted so that they will appear in the
 * document verbatim. Markdown documents are converted to AsciiDoc for the display.
 * <p>
 * At the same time the saved file, the plugin generates chopping off the '.jam' file extension is NOT converted.
 */
public interface Converter {

    /**
     * Convert the original format to AsciiDoc so that the document will be displayed properly.
     * <p>
     * Note that this conversion is only used to generate the in-memory document that is displayed in the editor.
     * The processed file is saved as is, without any conversion.
     *
     * @param original the text in original format
     * @return the converted document
     */
    List<String> convert(List<String> original);

    /**
     * Should return {@code true} if the class can handle the file and can convert a file to AsciiDoc.
     * The full file name is given, though the decision is usually based on the file extension.
     *
     * @param fileName the name of the input file that is to be converted.
     * @return {@code true} if the implementation is ready to convert the file to AsciiDoc, {@code false} otherwise.
     */
    boolean canConvert(final String fileName);

    /**
     * Get all the instances of the converters that are available in the class path of the context class loader.
     *
     * @return the list of instances of the converters
     */
    static List<Converter> getInstances() {
        return getInstances(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Get all the instances of the converters that are available in the class path of the given class loader.
     * <p>
     * This method is used by the plugin to load the converters.
     *
     * @param cl the class loader to use to load the converters
     * @return the list of instances of the converters
     */
    static List<Converter> getInstances(final ClassLoader cl) {
        ServiceLoader<Converter> services = ServiceLoader.load(Converter.class, cl);
        final var list = new ArrayList<Converter>();
        services.iterator().forEachRemaining(list::add);
        if (list.isEmpty()) {
            ServiceLoaded.loadViaMetaInf(Converter.class, list, cl);
        }
        return list;
    }
}
