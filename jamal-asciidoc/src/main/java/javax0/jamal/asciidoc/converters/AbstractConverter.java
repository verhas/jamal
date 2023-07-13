package javax0.jamal.asciidoc.converters;

import javax0.jamal.asciidoc.Converter;

import java.util.List;

public abstract class AbstractConverter implements Converter {

    /**
     * Return the file extensions that the concrete class can handle. The elementd do not inlclude the "." before the
     * extension and the trailing ".jam".
     *
     * @return the list of extensions the concrete implementation can handle.
     */
    abstract List<String> extensions();

    @Override
    public boolean canConvert(final String fileName) {
        for (final var extension : extensions()) {
            if (fileName.endsWith("." + extension + ".jam")) {
                return true;
            }
        }
        return false;
    }
}
