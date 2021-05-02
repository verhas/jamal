package javax0.jamal.test.yaml;

import javax0.jamal.DocumentConverter;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import org.junit.jupiter.api.Test;

public class TestConvertYamlReadme {

    @Test
    void generateDoc() throws Exception {
        DocumentConverter.convert("../jamal-yaml/README.adoc.jam");
    }

}
