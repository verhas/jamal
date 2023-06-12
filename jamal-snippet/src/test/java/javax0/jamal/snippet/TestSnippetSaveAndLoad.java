package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static javax0.jamal.DocumentConverter.getRoot;

public class TestSnippetSaveAndLoad {

    private final String root;

    public TestSnippetSaveAndLoad() throws IOException {
        this.root = getRoot();
    }


    @Test
    @DisplayName("Test that snippets can be saved as XML and then they can be loaded")
    void testSaveAllLoadAll() throws Exception {
        final var expected = Arrays.stream(TestThat
                .theInput("" +
                        "{@snip:collect from=\"src/main/java/\"}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results().split("\n")).sorted().collect(Collectors.joining("\n"));
        final var result = Arrays.stream(TestThat
                .theInput("" +
                        "{@snip:collect from=\"src/main/java/\"}" +
                        "{@snip:save output=target/dump1.xml}" +
                        "{@snip:clear}" +
                        "{@snip:load input=target/dump1.xml}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results().split("\n")).sorted().collect(Collectors.joining("\n"));
        Assertions.assertEquals(expected,result);
    }

    @Test
    @DisplayName("Test that snippets can be saved as XML and then they can be loaded matching the text regex")
    void testLoadSomeTextFilter() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump1.xml contains=margin}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results("" +
                        "trimLineStart");
    }

    @Test
    @DisplayName("Test that snippets can be loaded without hash code in the XML file")
    void testLoadWithoutHash() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_ohne_hash.xml}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results("" +
                        "is");
    }

    @Test
    @DisplayName("Test that snippets can be loaded matching the file name regex")
    void testSaveAllLoadSomeFileNameFilter() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump1.xml fileName=SnippetStore}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results("" +
                        "store");
    }

    @Test
    @DisplayName("Test that snippets can be loaded from a resource")
    void testLoadAllFromResource() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump1.xml}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results("" +
                        "is\n" +
                        "trimLineStart\n" +
                        "store\n" +
                        "dirMacroFormatPlaceholders\n" +
                        "fileMacroFormatPlaceholders\n" +
                        "collect_options\n" +
                        "defaultTimeForListDir\n" +
                        "listDirFormats\n" +
                        "classFormats\n" +
                        "fieldFormats\n" +
                        "methodFormats\n" +
                        "SnipCheck_MIN_LINE");
    }

    @Test
    @DisplayName("Test that snippet subset can be saved as XML and then they can be loaded")
    void testSaveSomeLoadAll() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:collect from=\"src/main/java/\"}" +
                        "{@snip:save output=target/dump2.xml name=fileMacroFormatPlaceholders}" +
                        "{@snip:clear}" +
                        "{@snip:load input=target/dump2.xml}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results("" +
                        "fileMacroFormatPlaceholders");
    }

    @Test
    @DisplayName("Test that snippets can be saved as XML and then a subset can be loaded")
    void testSaveAllLoadSome() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:collect from=\"src/main/java/\"}" +
                        "{@snip:save output=target/dump3.xml}" +
                        "{@snip:clear}" +
                        "{@snip:load input=target/dump3.xml name=fileMacroFormatPlaceholders}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results("" +
                        "fileMacroFormatPlaceholders");
    }

    @Test
    @DisplayName("snip:save throws up for a bad format")
    void testSaveThrows() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:collect from=\"src/main/java/\"}" +
                        "{@snip:save output=target/dump4.xml format=abrakaDabra}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("The only supported format is XML");
    }

    @Test
    @DisplayName("snip:save throws up for a bad format")
    void testLoadThrowsBadHash() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_bad_hash.xml}"
                )
                .throwsBadSyntax("The 'hash'.*");
    }

    @Test
    @DisplayName("snip:save throws up for a bad file name")
    void testSaveThrowsForBadFileName() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:collect from=\"src/main/java/\"}" +
                        "{@snip:save output=res:target/dump4.xml format=XML}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                // cant say target/dump4.xml because it fails on Windows
                .throwsBadSyntax();
    }

    @Test
    @DisplayName("snip:load throws for wrong format")
    void testSaveLoadThrows() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:collect from=\"src/main/java/\"}" +
                        "{@snip:save output=target/dump5.xml}" +
                        "{@snip:clear}" +
                        "{@snip:load input=target/dump5.xml format=\"abraka dabra\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("The only supported format is XML");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the top level namespace is wrong")
    void testLoadFailBadXmlRootNS() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_bad_namespace.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("The root element of the XML document must be.*");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the top level namespace is wrong")
    void testLoadFailBadXmlRootTag() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_bad_root_tag.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("The root element of the XML document must be.*");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the child level tag is not 'snippet'")
    void testLoadFailBadXmlChildTag() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_bad_child_tag.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("XML document must contain only 'snippet' tags under the 'snippets' root element");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the child level has no 'id'")
    void testLoadFailBadXmlChildTagNoId() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_no_id_attr.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("Could not read or parse the XML file");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the child level has no 'file'")
    void testLoadFailBadXmlChildTagNoFile() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_no_file.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("Could not read or parse the XML file");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the child level has no 'line'")
    void testLoadFailBadXmlChildTagNoLine() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_no_line.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("Could not read or parse the XML file");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the child level has no 'column'")
    void testLoadFailBadXmlChildTagNoColumn() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_no_column.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("Could not read or parse the XML file");
    }


    @Test
    @DisplayName("Test that snippets cannot be loaded when the child level has bad 'line'")
    void testLoadFailBadXmlChildTagBadLine() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_bad_line.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("Could not read or parse the XML file");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the child level has bad 'column'")
    void testLoadFailBadXmlChildTagBadColumn() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_bad_column.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("Could not read or parse the XML file");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the text is not in CDATA")
    void testLoadFailBadXmlChildTagBadCData() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_bad_cdata.xml}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .throwsBadSyntax("Could not read or parse the XML file");
    }

    @Test
    @DisplayName("Test that snippets cannot be loaded when the text is not in CDATA")
    void testLoadCDataWithCommentAndText() throws Exception {
        TestThat
                .theInput("" +
                        "{@snip:clear}" +
                        "{@snip:load input=res:javax0/jamal/snippet/dump_cdata_w_newline_before_after.xml}" +
                        "{@snip:list listSeparator=\"\\n\"}"
                )
                .atPosition(root + "/jamal-snippet/README.adoc.jam", 1, 1)
                .results("identifier");
    }

    @Test
    @DisplayName("Challenge CDATA closing tag")
    void testCDATA() throws Exception {
        TestThat.theInput("" +
                "{@snip:define cdata=]]>}" +
                "{@snip:save output=target/cdata.xml}" +
                "{@snip:clear}" +
                "{@snip:load input=target/cdata.xml}" +
                "{@snip cdata}"
        ).results("]]>");
    }
}
