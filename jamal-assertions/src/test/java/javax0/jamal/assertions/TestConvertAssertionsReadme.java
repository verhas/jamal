package javax0.jamal.assertions;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertAssertionsReadme {

    @Test
    void convertAssertionsReadme() throws Exception {
        DocumentConverter.convert("../jamal-assertions/README.adoc.jam");
    }
}
