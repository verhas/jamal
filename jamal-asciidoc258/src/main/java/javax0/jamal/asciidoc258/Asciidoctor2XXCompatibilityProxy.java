package javax0.jamal.asciidoc258;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

public class Asciidoctor2XXCompatibilityProxy extends Preprocessor {


    private final CompatibilityProcess process;

    /**
     * Return a version of the process that is compatible with the version of the asciidoctor that is used.
     *
     * @param process the process compatible with the versions 3XX and to be invoked by the version compatible with 2XX
     * @return either the original process or a proxy that is compatible with the version of the asciidoctor
     */
    public static Object create(CompatibilityProcess process) {
        if (isVersion2XX()) {
            return new Asciidoctor2XXCompatibilityProxy(process);
        } else {
            return process;
        }
    }

    /**
     * Check if the version of the asciidoctor is 2XX or not.
     * <p>
     * The difference between the 2XX and 3XX versions is that the process method in the Preprocessor class has a
     * different signature. In 2XX the method returns void, in 3XX it returns Reader.
     *
     * @return true if the version is 2XX
     * @implNote This method uses reflection to check the version. It is not the most efficient way to do it, but it is
     * the most reliable way.
     */
    private static boolean isVersion2XX() {
        try {
            final var abstractPreprocessor = Class.forName("org.asciidoctor.extension.Preprocessor");
            for (final var m : abstractPreprocessor.getDeclaredMethods()) {
                if ("process".equals(m.getName())) {
                    return m.getReturnType() == void.class;
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    private Asciidoctor2XXCompatibilityProxy(CompatibilityProcess process) {
        super();
        this.process = process;
    }

    /**
     * This method is called by the asciidoctor when the version is 2XX. It calls the process method of the 3XX
     * compatible version and ignores the return value.
     *
     * @param document the document
     * @param reader   the reader
     */
    @Override
    public void process(Document document, PreprocessorReader reader) {
        process.process(document, reader);
    }
}