package javax0.jamal.poi.word;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertWordReadme {

    @Test
    void generateDoc() throws Exception {
        DocumentConverter.convert("./README.adoc.jam");
    }
}
