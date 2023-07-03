package javax0.jamal.asciidoc258;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.extension.Reader;

public interface CompatibilityProcess {
    Reader process(Document document, PreprocessorReader reader);
}