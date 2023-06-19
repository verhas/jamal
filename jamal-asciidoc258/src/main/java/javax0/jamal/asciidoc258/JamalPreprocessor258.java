package javax0.jamal.asciidoc258;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.extension.Reader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JamalPreprocessor258 extends Preprocessor {
    public interface Process {
        Reader process(Document document, PreprocessorReader reader);
    }

    private final Process process;

    public JamalPreprocessor258() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super();
        process = (Process) Class.forName("javax0.jamal.asciidoc.JamalPreprocessor").getConstructor().newInstance();
    }

    public void process(Document document, PreprocessorReader reader) {
        try {
            process.process(document, reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}