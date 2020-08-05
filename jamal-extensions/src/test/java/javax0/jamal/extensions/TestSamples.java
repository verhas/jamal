package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSamples {
    private javax0.jamal.api.Input createInput(String testFile) throws IOException {
        var fileName = this.getClass().getResource(testFile).getFile();
        fileName = fixupPath(fileName);
        var fileContent = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        return new Input(fileContent, new Position(fileName));
    }

    /**
     * Fixup the JDK bug JDK-8197918
     *
     * @param fileName the file name that may contain an erroneous leading / on Windows
     * @return the file without the leading / if it contains ':', so it is assumed this is Windows
     */
    private String fixupPath(String fileName) {
        if (fileName.contains(":")) {
            fileName = fileName.substring(1);
        }
        return fileName;
    }

    private String result(String testFile) throws IOException, BadSyntax {
        var in = createInput(testFile);
        final var sut = new Processor("{", "}");
        return sut.process(in);
    }

    @Test
    @DisplayName("simple import")
    void testSimpleImport() throws IOException, BadSyntax {
        assertEquals("thisIsGood\n" +
                "ThisIsAlsoGood\n" +
                "THIS_IS_EXTRA_GOOD\n" +
                "this is extra good\n" +
                "This is extra good.", result("use.jam"));
    }

    @Test
    @DisplayName("for loop iterates through the elements")
    void testForLoop() throws IOException, BadSyntax {
        assertEquals(" a is either a, b, c or d\n" +
            " b is either a, b, c or d\n" +
            " c is either a, b, c or d\n" +
            " d is either a, b, c or d\n" +
            " a is either a, b, c or d\n" +
            " b is either a, b, c or d\n" +
            " c is either a, b, c or d\n" +
            " d is either a, b, c or d\n", result("for.jam"));
    }

    @Test
    @DisplayName("matcher generates the groups")
    void testMatcherLoop() throws IOException, BadSyntax {
        assertEquals("\n\n" +
            "true\n" +
            "2\n" +
            "1. before the slash\n" +
            "2. after the slash\n", result("matcher.jam"));
    }

    final static String SNIPPET = "\n" +
        "\n" +
        "```\n" +
        "    @Test\n" +
        "    @DisplayName(\"snippets can be included from files\")\n" +
        "    void testSnippetInclusion() throws IOException, BadSyntax {\n" +
        "        assertEquals(SNIPPET, result(\"snippet_test.txt.jam\"));\n" +
        "    }\n" +
        "```";

    // snippet     testSnippetInclusion
    @Test
    @DisplayName("snippets can be included from files")
    void testSnippetInclusion() throws IOException, BadSyntax {
        assertEquals(SNIPPET, result("snippet_test.txt.jam"));
    }
    // snippet end

}
