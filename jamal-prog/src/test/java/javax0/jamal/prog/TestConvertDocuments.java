package javax0.jamal.prog;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertDocuments {

    @Test
    void convertProgReadme() throws Exception {
        DocumentConverter.convert("../jamal-prog/README.adoc.jam");
    }
}
