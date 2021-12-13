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
                "</a>\n");
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
                "</a>\n");
    }


    @Test
    @DisplayName("XmlFormat can convert thin XML to thick XML")
    void testThinXmlFormat() throws Exception {
        TestThat.theInput("{@xmlFormat (thin)" +
                "apple core=hard chew=\"bak\\\"ka\">\n" +
                "            plum>\n" +
                "                              this is just text &gt; under plum\n" +
                "        here plum is closed and this is another text\n" +
                "\n" + // an empty line
                "     pineApple>is another fine fruit that goes well with pizz...aaarghhh\n" +
                "         <![CDATA[\n" +
                "    this is a CDATA section\n a few lines\n  ever more tabbed\nand untabbed\n" +
                "]]>}")
            .ignoreLineEnding()
            .results("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<apple chew=\"bak&quot;ka\" core=\"hard\">\n" +
                "    <plum>\n" +
                "        this is just text &gt; under plum\n" +
                "    </plum>\n" +
                "        here plum is closed and this is another text\n" +
                "\n" +
                "    \n" +
                "    <pineApple>is another fine fruit that goes well with pizz...aaarghhh<![CDATA[\n" +
                "    this is a CDATA section\n" +
                " a few lines\n" +
                "  ever more tabbed\n" +
                "and untabbed\n" +
                "]]></pineApple>\n" +
                "</apple>\n"
            );
    }

    @Test
    @DisplayName("XmlFormat can convert thin XML to thick XML deferred")
    void testThinXmlFormatDeferred() throws Exception {
        TestThat.theInput("{@xmlFormat (thin)}" +
                "apple core=hard chew=\"bak\\\"ka\">\n" +
                "             plum>\n" +
                "                              this is just text &gt; under plum\n" +
                "        here plum is closed and this is another text\n" +
                "\n" + // an empty line
                "     pineApple>is another fine fruit that goes well with pizz...aaarghhh\n" +
                "       <![CDATA[\n" +
                "    this is a CDATA section\n a few lines\n  ever more tabbed\nand untabbed\n" +
                "]]>")
            .ignoreLineEnding()
            .results("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<apple chew=\"bak&quot;ka\" core=\"hard\">\n" +
                "    <plum>\n" +
                "        this is just text &gt; under plum\n" +
                "    </plum>\n" +
                "        here plum is closed and this is another text\n" +
                "\n" +
                "    \n" +
                "    <pineApple>is another fine fruit that goes well with pizz...aaarghhh<![CDATA[\n" +
                "    this is a CDATA section\n" +
                " a few lines\n" +
                "  ever more tabbed\n" +
                "and untabbed\n" +
                "]]></pineApple>\n" +
                "</apple>\n"
            );
    }
}
