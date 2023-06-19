package javax0.jamal.asciidoc258;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.extension.Reader;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class JamalPreprocessor258 extends Preprocessor {
    public interface Process {
        Reader process(Document document, PreprocessorReader reader);
    }

    private final Process process;

    public JamalPreprocessor258(Process process) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super();
        this.process = process;
    }

    public void process(Document document, PreprocessorReader reader) {
        process.process(document, reader);
    }
}