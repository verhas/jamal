package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestXmlFormat {

    @Test
    void testXmlFormat() throws Exception {
        TestThat.theInput("{#xmlFormat {@define tabsize=2}\n" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<a>\n        <b attribes=\"wuff\">b text</b>\n</a>}")
            .results("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<a>\n" +
                "  <b attribes=\"wuff\">b text</b>\n" +
                "</a>");
    }
}
