package javax0.jamal.doclet;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.Debugger;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import org.junit.jupiter.api.Test;

public class TestConvertDocletReadme {

    @Test
    void generateDoc() throws Exception {
        DocumentConverter.convert("./README.adoc.jam");
    }

}
