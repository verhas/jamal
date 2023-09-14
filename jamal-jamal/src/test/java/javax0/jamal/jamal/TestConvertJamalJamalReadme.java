package javax0.jamal.jamal;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertJamalJamalReadme {

    @Test
    void generateDoc() throws Exception {
        DocumentConverter.convert("./README.adoc.jam");
    }

}
