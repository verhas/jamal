package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSamples {


    private javax0.jamal.api.Input createInput(String testFile) throws IOException {
        var fileName = this.getClass().getResource(testFile).getFile();
        fileName = fixupPath(fileName);
        var fileContent = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        return new Input(new StringBuilder(fileContent), fileName);
    }

    /**
     * Fixup the JDK bug JDK-8197918
     *
     * @param fileName the file name that may contain an erroneous leading / on Windows
     * @return the fileName without the leading / if it contains ':', so it is assumed this is Windows
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
    public void testSimpleImport() throws IOException, BadSyntax {
        assertEquals("almakorte", result("test_import.jam"));
    }

    @Test
    @DisplayName("import definitions")
    public void testImportDefine() throws IOException, BadSyntax {
        assertEquals("xxx is defined already", result("import_defines.jam"));
    }

    @Test
    @DisplayName("simple include")
    public void testSimpleInclude() throws IOException, BadSyntax {
        assertEquals("alma szilva korte", result("test_include.jam"));
    }

    @Test
    @DisplayName("include definitions")
    public void testIncludeDefine() throws IOException, BadSyntax {
        assertEquals("** **", result("include_defines.jam"));
    }

    @Test
    @DisplayName("include global definitions")
    public void testIncludeGlobalDefine() throws IOException, BadSyntax {
        assertEquals("* belzebub", result("include_global_defines.jam"));
    }

    @Test
    @DisplayName("define with parameters")
    public void testDefineWithParameters() throws IOException, BadSyntax {
        assertEquals("ttt_ttt\nttt_ttt", result("define_with_parameters.jam"));
    }

    @Test
    @DisplayName("testsupport exporting")
    public void testExport() throws IOException, BadSyntax {
        assertEquals("defined exported", result("test_export.jam"));
    }

    @Test
    @DisplayName("testsupport separator setting")
    public void testSeparator() throws IOException, BadSyntax {
        assertEquals("121", result("test_sep.jam"));
    }

    @Test
    @DisplayName("testsupport script evaluation")
    public void testScript() throws IOException, BadSyntax {
        assertEquals("11", result("script.jam"));
    }

    @Test
    @DisplayName("import only selected macros")
    public void testSelectedImport() throws IOException, BadSyntax {
        assertEquals("\n2468", result("import_only_selected.jam"));
    }

    @Test
    @DisplayName("testsupport script evaluation")
    public void testScriptComplex() throws IOException, BadSyntax {
        assertEquals("1. this is the text that we will repeat two times\n" +
            "2. this is the text that we will repeat two times\n", result("script_complex.jam"));
    }

    @Test
    @DisplayName("testsupport script include")
    public void testScriptInclude() throws IOException, BadSyntax {
        assertEquals("11", result("test_script.jam"));
    }

    @Test
    @DisplayName("testsupport eval")
    public void testEval() throws IOException, BadSyntax {
        assertEquals("apple", result("eval.jam"));
    }

    @Test
    @DisplayName("run documentation script")
    public void testDocumentation() throws IOException, BadSyntax {
        var fileName = this.getClass().getResource("documentation.out.jam").getFile();
        fileName = fixupPath(fileName);
        var fileContent = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        assertEquals(fileContent, result("documentation.jam"));
    }

    @Test
    @DisplayName("complex sep use with restore")
    public void testSepComplex() throws IOException, BadSyntax {
        assertEquals("\n" +
            "\n" +
            "\n" +
            "zazizazi[[?z]]", result("sep_complex.jam"));
    }

    @Test
    @DisplayName("ident protects argument from post evaluation")
    public void testIdent() throws IOException, BadSyntax {
        assertEquals("\n\n\n\n{a}\n13\n{a}", result("ident.jam"));
    }

    @Test
    @DisplayName("begin and end works properly")
    public void testBeginEnd() throws IOException, BadSyntax {
        assertEquals("212", result("begin.jam"));
    }

    @Test
    @DisplayName("sep that defines zero length macro open throws exception")
    public void testBadSep() throws IOException, BadSyntax {
        Assertions.assertThrows(BadSyntax.class, () -> result("badsep.jam"));
    }

    @Test
    @DisplayName("sep that defines zero length macro close throws exception")
    public void testBadSep2() throws IOException, BadSyntax {
        Assertions.assertThrows(BadSyntax.class, () -> result("badsep2.jam"));
    }

    @Test
    @DisplayName("when the input has zero character after { it is handled")
    public void zeroLengthInput() throws IOException, BadSyntax {
        Assertions.assertThrows(BadSyntax.class, () -> result("zeroinput.jam"));
    }

}
