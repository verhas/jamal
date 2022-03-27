package javax0.jamal.markdown;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertMarkdownReadme {
    @Test
    void convertMarkdownReadme() throws Exception {
        DocumentConverter.convert("./README.adoc.jam");
    }
}
