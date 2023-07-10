package javax0.jamal.poi.word;

import org.junit.jupiter.api.Test;

public class TestXWPFProcessor {

    @Test
    void testSample() throws Exception {
        final var sut = new XWPFProcessor();
        sut.process("src/test/resources/sample.docx", "src/test/resources/sampleConverted.docx");
    }

    @Test
    void testInclude() throws Exception {
        final var sut = new XWPFProcessor();
        sut.process("src/test/resources/includetest.docx", "src/test/resources/includetestConverted.docx");
    }

    @Test
    void testPicture() throws Exception {
        final var sut = new XWPFProcessor();
        sut.process("src/test/resources/picture.docx", "src/test/resources/pictureConverted.docx");
    }

    @Test
    void testDemo() throws Exception {
        final var sut = new XWPFProcessor("{%", "%}");
        sut.process("src/test/resources/demo.docx", "src/test/resources/demoConverted.docx");
    }
}


