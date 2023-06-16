package javax0.jamal.asciidoc258;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JamalPreprocessor258 extends Preprocessor {

    private final Object proxy;
    private Method method = null;

    public JamalPreprocessor258() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super();
        proxy = Class.forName("javax0.jamal.asciidoc.JamalPreprocessor").getConstructor().newInstance();
        for (final var m : proxy.getClass().getDeclaredMethods()) {
            if (m.getName().equals("process") ) {
                method = m;
                break;
            }
        }
    }

    public void process(Document document, PreprocessorReader reader) {
        try {
            method.invoke(proxy, document, reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}