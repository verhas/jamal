package javax0.jamal.io;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertIoReadme {

    @Test
    void generateDoc() throws Exception {
        DocumentConverter.convert("./README.adoc.jam");
    }

}
