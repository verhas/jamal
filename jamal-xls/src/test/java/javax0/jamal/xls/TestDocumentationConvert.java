package javax0.jamal.xls;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestDocumentationConvert {

    @Test
    void testConvertReadmeAdoc()throws Exception {
        final var root = DocumentConverter.getRoot();
        final var in = root + "/jamal-xls/README.adoc.jam";
        final var out = root + "/jamal-xls/README.adoc";
        //DocumentConverter.convert(in, out);
    }

}
