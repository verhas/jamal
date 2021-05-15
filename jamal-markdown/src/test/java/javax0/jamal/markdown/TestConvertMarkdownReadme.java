package javax0.jamal.markdown;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Test;

public class TestConvertMarkdownReadme {
    @Test
    void convertMarkdownReadme() throws Exception {
        DocumentConverter.convert("./README.adoc.jam");
    }
}
