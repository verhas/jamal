package javax0.jamal.poi.word;

import org.junit.jupiter.api.Test;

public class TestXWPFProcessor {

    @Test
    void testSample() throws Exception {
        final var sut = new XWPFProcessor();
        sut.process("src/test/resources/sample.docx", "src/test/resources/sampleConverted.docx");
    }
}


