package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSamples {


    private javax0.jamal.api.Input createInput(String testFile) throws IOException {
        var fileName = this.getClass().getResource(testFile).getFile();
        var fileContent = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        return new Input(new StringBuilder(fileContent), fileName);
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
        assertEquals("ttt_ttt", result("define_with_parameters.jam"));
    }

    @Test
    @DisplayName("test exporting")
    public void testExport() throws IOException, BadSyntax {
        assertEquals("defined exported", result("test_export.jam"));
    }

    @Test
    @DisplayName("test separator setting")
    public void testSeparator() throws IOException, BadSyntax {
        assertEquals("121", result("test_sep.jam"));
    }

    @Test
    @DisplayName("test script evaluation")
    public void testScript() throws IOException, BadSyntax {
        assertEquals("11", result("script.jam"));
    }

    @Test
    @DisplayName("test script evaluation")
    public void testScriptComplex() throws IOException, BadSyntax {
        assertEquals("1. this is the text that we will repeat two times\n" +
            "2. this is the text that we will repeat two times\n", result("script_complex.jam"));
    }

    @Test
    @DisplayName("test script include")
    public void testScriptInclude() throws IOException, BadSyntax {
        assertEquals("11", result("test_script.jam"));
    }

    @Test
    @DisplayName("test eval")
    public void testEval() throws IOException, BadSyntax {
        assertEquals("apple", result("eval.jam"));
    }

    @Test
    @DisplayName("run documentation script")
    public void testDocumentation() throws IOException, BadSyntax {
        var fileName = this.getClass().getResource("documentation.out.jam").getFile();
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
}
