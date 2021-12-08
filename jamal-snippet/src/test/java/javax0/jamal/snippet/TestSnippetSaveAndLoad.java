package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestSnippetSaveAndLoad {

    @Test
    @DisplayName("Test that snippets can be saved as XML and then they can be loaded")
    void test() throws Exception {
        final var root = TestFilesMacro.getDirectory();
        TestThat
            .theInput("" +
                "{@snip:collect from=\"src/main/java/\"}"+
                "{@snip:save output=target/README.snippets.xml}" +
                "{@snip:clear}" +
                "{@snip:load input=target/README.snippets.xml}" +
                "{@snip:list listSeparator=\"\\n\"}"
            )
            .atPosition(root +"/jamal-snippet/README.adoc.jam",1,1)
            .results("is\n" +
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
}
