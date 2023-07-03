package javax0.jamal.asciidoc258;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

public class Asciidoctor2XXCompatibilityProxy extends Preprocessor {


    private final CompatibilityProcess process;

    public static Object create(CompatibilityProcess process) {
        try {
            final var abstractPreprocessor = Class.forName("org.asciidoctor.extension.Preprocessor");
            for (final var m : abstractPreprocessor.getDeclaredMethods()) {
                if ("process".equals(m.getName())) {
                    if (m.getReturnType() == void.class) {
                        return new Asciidoctor2XXCompatibilityProxy(process);
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return process;
    }

    private Asciidoctor2XXCompatibilityProxy(CompatibilityProcess process) {
        super();
        this.process = process;
    }

    public void process(Document document, PreprocessorReader reader) {
        process.process(document, reader);
    }
}