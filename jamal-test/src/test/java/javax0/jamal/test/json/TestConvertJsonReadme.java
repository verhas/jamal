package javax0.jamal.test.json;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertJsonReadme {

    @Test
    void generateDoc() throws Exception {
        DocumentConverter.convert("../jamal-json/README.adoc.jam");
    }

}
