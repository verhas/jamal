package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestXmlFormat {

    @Test
    @DisplayName("XmlFormat formats its argument")
    void testXmlFormat() throws Exception {
        TestThat.theInput("{#xmlFormat {@define tabsize=2}\n" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<a>\n        <b attribes=\"wuff\">b text</b>\n</a>}")
            .ignoreLineEnding()
            .results("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<a>\n" +
                "  <b attribes=\"wuff\">b text</b>\n" +
                "</a>");
    }

    @Test
    @DisplayName("XmlFormat formats the whole result at the end")
    void testXmlFormatPostProcess() throws Exception {
        TestThat.theInput(
            "{#xmlFormat {@define tabsize=2}}\n" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<a>\n        <b attribes=\"wuff\">b text</b>\n</a>")
            .ignoreLineEnding()
            .results("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<a>\n" +
                "  <b attribes=\"wuff\">b text</b>\n" +
                "</a>");
    }
}
