package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax0.jamal.tools.Input.makeInput;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestSamples {

    private Input createInput(String testFileName) throws IOException {
        var fileName = Objects.requireNonNull(this.getClass().getResource(testFileName), "File '" + testFileName + "' does not exist").getFile();
        fileName = fixupPath(fileName);
        try (final var lines = Files.lines(Paths.get(fileName))) {
            var fileContent = lines.collect(Collectors.joining("\n"));
            return makeInput(fileContent, new Position(fileName));
        }
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

    /**
     * Processes the text file and creates the resulting file in the resources directory so it can be examined.
     *
     * @param testFileName the test file name
     * @return the evaluated result
     * @throws IOException when the file cannot be read
     * @throws BadSyntax   when there is syntax error in the test file
     */
    private String result(String testFileName) throws IOException, BadSyntax {
        var in = createInput(testFileName);
        final var sut = new Processor("{", "}");
        return sut.process(in);
    }

    @Test
    @DisplayName("simple import")
    void testSimpleImport() throws IOException, BadSyntax {
        assertEquals("almakorte", result("test_import.jam"));
    }

    @Test
    @DisplayName("import definitions")
    void testImportDefine() throws IOException, BadSyntax {
        assertEquals("xxx is defined already", result("import_defines.jam"));
    }

    @Test
    @DisplayName("simple include")
    void testSimpleInclude() throws IOException, BadSyntax {
        assertEquals("alma szilva korte", result("test_include.jam"));
    }

    @Test
    @DisplayName("include definitions")
    void testIncludeDefine() throws IOException, BadSyntax {
        assertEquals("** **", result("include_defines.jam"));
    }

    @Test
    @DisplayName("include global definitions")
    void testIncludeGlobalDefine() throws IOException, BadSyntax {
        assertEquals("* belzebub", result("include_global_defines.jam"));
    }

    @Test
    @DisplayName("testsupport script evaluation")
    void testScript() throws IOException, BadSyntax {
        if (Runtime.version().feature() < 15) {
            assertEquals("11", result("script.jam"));
        }
    }

    @Test
    @DisplayName("throws BadSyntax when there is n")
    void testScriptWrongScriptEngine() {
        assertThrows(BadSyntax.class, () -> result("script_wrong_script_engine.jam"));
    }

    @Test
    @DisplayName("import only selected macros")
    void testSelectedImport() throws IOException, BadSyntax {
        assertEquals("\n2468", result("import_only_selected.jam"));
    }

    @Test
    @DisplayName("testsupport script evaluation")
    void testScriptComplex() throws IOException, BadSyntax {
        if (Runtime.version().feature() < 15) {
            assertEquals("1. this is the text that we will repeat two times\n" +
                "2. this is the text that we will repeat two times\n", result("script_complex.jam"));
        }
    }

    @Test
    @DisplayName("throws BadSyntax when script engine is not found")
    void testEvalNoEngine() {
        assertThrows(BadSyntax.class, () -> result("eval_wrong_script_engine.jam"));
    }

    @Test
    @DisplayName("testsupport eval")
    void testEval() throws IOException, BadSyntax {
        if (Runtime.version().feature() < 15) {
            assertEquals("apple", result("eval.jam"));
        }
    }
}
