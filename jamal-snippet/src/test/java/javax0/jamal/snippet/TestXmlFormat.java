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
            .ignoreLineEnding().ignoreSpaces()
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
    @DisplayName("thinXml can convert thin XML to thick XML")
    void testThinXmlNonFormat() throws Exception {
        TestThat.theInput("{@thinXml\n" +
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
                "<apple core=\"hard\" chew=\"bak&quot;ka\">\n" +
                "    <plum>\n" +
                "        this is just text &gt; under plum\n" +
                "    </plum>\n" +
                "    here plum is closed and this is another text\n" +
                "\n" +
                "    <pineApple>is another fine fruit that goes well with pizz...aaarghhh<![CDATA[\n" +
                "    this is a CDATA section\n" +
                " a few lines\n" +
                "  ever more tabbed\n" +
                "and untabbed\n" +
                "]]>\n" +
                "</pineApple>\n" +
                "</apple>\n"
            );
    }

    @Test
    @DisplayName("thinXml can convert thin XML to thick XML with non-empty-blank line")
    void testThinXmlWNonEmptyBlankLine() throws Exception {
        TestThat.theInput("{#thinXml\n" +
                "project>\n" +
                "    artifactId>lambda-final\n" +
                "    version>1.0.0-SNAPSHOT\n" +
                "    dependencies>\n" +
                "        {@define JAMAL_VERSION=1.10.3}\n" +
                "        dependency>\n" +
                "            groupId>com.javax0.jamal\n" +
                "            artifactId>jamal-api\n" +
                "            version>{JAMAL_VERSION}\n" +
                "            scope>test}")
            .ignoreLineEnding()
            .results("" +
                "<project>\n" +
                "    <artifactId>lambda-final</artifactId>\n" +
                "    <version>1.0.0-SNAPSHOT</version>\n" +
                "    <dependencies>\n" +
                "\n" +
                "        <dependency>\n" +
                "            <groupId>com.javax0.jamal</groupId>\n" +
                "            <artifactId>jamal-api</artifactId>\n" +
                "            <version>1.10.3</version>\n" +
                "            <scope>test</scope>\n" +
                "        </dependency>\n" +
                "    </dependencies>\n" +
                "</project>\n"
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
            .ignoreLineEnding().ignoreSpaces()
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
