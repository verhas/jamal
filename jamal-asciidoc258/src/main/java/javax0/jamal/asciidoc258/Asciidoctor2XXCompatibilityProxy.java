package javax0.jamal.asciidoc258;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

public class Asciidoctor2XXCompatibilityProxy extends Preprocessor {


    private final CompatibilityProcess process;

    /**
     * Creates a compatibility proxy for Asciidoctor processing
     * if the specified process object's class has a method named "process"
     * with a void return type.
     * This is intended for ensuring compatibility with Asciidoctor 2.X versions.
     *
     * <p>
     * The method dynamically checks if the "org.asciidoctor.extension.Preprocessor" class has a declared method named "process"
     * with a void return type. If such a method exists, it returns an instance of {@code Asciidoctor2XXCompatibilityProxy}
     * wrapped around the provided {@code process} object. If the method does not exist, is not accessible, or any exception occurs
     * during reflection, it simply returns the original {@code process} object.</p>
     *
     * @param process the object for which to create a compatibility proxy. It is expected that this object is
     *                an instance of a class that would benefit from compatibility handling.
     * @return an instance of {@code Asciidoctor2XXCompatibilityProxy} if the conditions are met, otherwise returns
     * the original {@code process} object.
     */
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