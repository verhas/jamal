package javax0.jamal.test.yaml;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertYamlReadme {

    @Test
    void generateDoc() throws Exception {
        DocumentConverter.convert("../jamal-yaml/README.adoc.jam");
    }

}
