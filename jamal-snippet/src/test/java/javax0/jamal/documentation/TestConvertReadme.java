package javax0.jamal.documentation;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

public class TestConvertReadme {

    @Test
    void convertCoreMacrosDocumentation() throws Exception {
        Files.walk(new java.io.File("../documentation/macros").toPath())
            .filter(p -> p.toString().endsWith(".adoc.jam"))
            .forEach(p -> {
                try {
                    DocumentConverter.convert(p.toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }
    @Test
    void convertSnippetArticle() throws Exception {
        System.setProperty("java.awt.headless", "true");
        DocumentConverter.convert("./ARTICLE.wp.jam");
    }

    @Test
    void convertSnippetArticle2() throws Exception {
        DocumentConverter.convert("./ARTICLE2.wp.jam");
    }

    @Test
    void convertWritingBuiltIn() throws Exception {
        DocumentConverter.convert("../BUILTIN.adoc.jam");
    }

    @Test
    void convertParams() throws Exception {
        DocumentConverter.convert("../documentation/PAROPS.adoc.jam");
    }

    @Test
    void convertGlossary() throws Exception {
        DocumentConverter.convert("../GLOSSARY.adoc.jam");
    }

    @Test
    void convertReleases() throws Exception {
        DocumentConverter.convert("../RELEASES.adoc.jam");
    }

    @Test
    void convertTopReadme() throws Exception {
        System.setProperty("java.awt.headless", "true");
        DocumentConverter.convert("../README.adoc.jam");
    }

    @Test
    void convertTopFAQ() throws Exception {
        DocumentConverter.convert("../FAQ.adoc.jam");
    }

    @Test
    void convertVideo() throws Exception {
        DocumentConverter.convert("../documentation/VIDEO.adoc.jam");
    }

   @Test
    void convertAsciiDocReadme() throws Exception {
        DocumentConverter.convert("../jamal-asciidoc/README.adoc.jam");
    }

    @Test
    void convertDocletReadme() throws Exception {
        DocumentConverter.convert("../jamal-doclet/README.adoc.jam");
    }

    @Test
    void convertAllReadme() throws Exception {
        DocumentConverter.convert("../jamal-all/README.adoc.jam");
    }

    @Test
    void convertCmdReadme() throws Exception {
        DocumentConverter.convert("../jamal-cmd/README.adoc.jam");
    }

    @Test
    void convertDebugReadme() throws Exception {
        DocumentConverter.convert("../jamal-debug/README.adoc.jam");
    }

    @Test
    void convertExtensionReadme() throws Exception {
        DocumentConverter.convert("../jamal-extensions/README.adoc.jam");
    }

    @Test
    void convertMavenExtensionReadme() throws Exception {
        System.setProperty("java.awt.headless", "true");
        DocumentConverter.convert("../jamal-maven-extension/README.adoc.jam");
    }

    @Test
    void convertMavenPluginReadme() throws Exception {
        DocumentConverter.convert("../jamal-maven-plugin/README.adoc.jam");
    }

    @Test
    void convertScriptBasicReadme() throws Exception {
        DocumentConverter.convert("../jamal-scriptbasic/README.adoc.jam");
    }

    @Test
    void convertTestReadme() throws Exception {
        DocumentConverter.convert("../jamal-test/README.adoc.jam");
    }

    @Test
    @DisplayName("Convert the Snippet module README.adoc.jam -> README.adoc")
    void convertSnippetReadme() throws Exception {
        DocumentConverter.convert("../jamal-snippet/README.adoc.jam");
    }

    @Test
    @DisplayName("Convert the Snippet module THINXML.adoc.jam -> THINXML.adoc")
    void convertThinXml() throws Exception {
        DocumentConverter.convert("../jamal-snippet/THINXML.adoc.jam");
    }
}
