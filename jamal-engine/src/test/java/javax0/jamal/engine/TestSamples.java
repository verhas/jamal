package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestSamples {

    private javax0.jamal.api.Input createInput(String testFileName) throws IOException {
        var fileName = Objects.requireNonNull(this.getClass().getResource(testFileName), "File '" + testFileName + "' does not exist").getFile();
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

    /**
     * Processes the text file and creates the resulting file in the resources directory so it can be examined.
     *
     * @param testFileName the test file name
     * @return
     * @throws IOException
     * @throws BadSyntax
     */
    private String result(String testFileName) throws IOException, BadSyntax {
        var in = createInput(testFileName);
        final var sut = new Processor("{", "}");
        final var res = sut.process(in);
        return res;
    }

    private String expected(String expectedFileName) throws IOException {
        var fileName = Objects.requireNonNull(this.getClass().getResource(expectedFileName), "File '" + expectedFileName + "' does not exist").getFile();
        fileName = fixupPath(fileName);
        try (final var is = new FileInputStream(fileName)) {
            final var bytes = is.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
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
    @DisplayName("define with parameters")
    void testDefineWithParameters() throws IOException, BadSyntax {
        assertEquals("ttt_ttt\nttt_ttt", result("define_with_parameters.jam"));
    }

    @Test
    @DisplayName("testsupport exporting")
    void testExport() throws IOException, BadSyntax {
        assertEquals("defined exported", result("test_export.jam"));
    }

    @Test
    @DisplayName("testsupport separator setting")
    void testSeparator() throws IOException, BadSyntax {
        assertEquals("121", result("test_sep.jam"));
    }

    @Test
    @DisplayName("testsupport script evaluation")
    void testScript() throws IOException, BadSyntax {
        assertEquals("11", result("script.jam"));
    }

    @Test
    @DisplayName("import only selected macros")
    void testSelectedImport() throws IOException, BadSyntax {
        assertEquals("\n2468", result("import_only_selected.jam"));
    }

    @Test
    @DisplayName("testsupport script evaluation")
    void testScriptComplex() throws IOException, BadSyntax {
        assertEquals("1. this is the text that we will repeat two times\n" +
                "2. this is the text that we will repeat two times\n", result("script_complex.jam"));
    }

    @Test
    @DisplayName("testsupport script include")
    void testScriptInclude() throws IOException, BadSyntax {
        assertEquals("11", result("test_script.jam"));
    }

    @Test
    @DisplayName("testsupport eval")
    void testEval() throws IOException, BadSyntax {
        assertEquals("apple", result("eval.jam"));
    }

    @Test
    @DisplayName("run documentation script")
    void testDocumentation() throws IOException, BadSyntax {
        var fileName = this.getClass().getResource("documentation.out.jam").getFile();
        fileName = fixupPath(fileName);
        var fileContent = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        assertEquals(fileContent, result("documentation.jam"));
    }

    @Test
    @DisplayName("complex sep use with restore")
    void testSepComplex() throws IOException, BadSyntax {
        assertEquals("\n" +
                "\n" +
                "\n" +
                "zazizazi[[?z]]", result("sep_complex.jam"));
    }

    @Test
    @DisplayName("a define is defined inside define")
    void testDefineDefine() throws IOException, BadSyntax {
        assertEquals("\n\n<name>Peter</name>", result("define_define.jam"));
    }

    @Test
    @DisplayName("ident protects argument from post evaluation")
    void testIdent() throws IOException, BadSyntax {
        assertEquals("\n\n\n\n{a}\n13\n{a}", result("ident.jam"));
    }

    @Test
    @DisplayName("test that the splitting works even with empty elements")
    void testSplitting() throws IOException, BadSyntax {
        assertEquals("\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "    <groupId>org.apache.maven.plugins</groupId><artifactId>maven-surefire-plugin</artifactId>\n" +
                "    ", result("gav.jam"));
    }

    @Test
    @DisplayName("begin and end works properly")
    void testBeginEnd() throws IOException, BadSyntax {
        assertEquals("212", result("begin.jam"));
    }

    @Test
    @DisplayName("sep that defines zero length macro open throws exception")
    void testBadSep() {
        Assertions.assertThrows(BadSyntaxAt.class, () -> result("badsep.jam"));
    }

    @Test
    @DisplayName("sep that defines zero length macro close throws exception")
    void testBadSep2() {
        Assertions.assertThrows(BadSyntaxAt.class, () -> result("badsep2.jam"));
    }

    @Test
    @DisplayName("when the input has zero character after { it is handled")
    void zeroLengthInput() {
        Assertions.assertThrows(BadSyntaxAt.class, () -> result("zeroinput.jam"));
    }

    @Test
    @DisplayName("Eval/jamal is evaluated properly")
    void testEvaluateJamal() throws BadSyntax, IOException {
        assertThrows(BadSyntaxAt.class, () -> result("ej_fail.jam"));
        assertEquals("\nzzzsss", result("eval_jamal.jam"));
    }

    @Test
    @DisplayName("Different 'if' statements are correctly evaluated")
    void testIf() throws BadSyntax, IOException {
        assertEquals("true=true\n" +
                "true=true\n" +
                "false=false\n" +
                "false=false\n" +
                "false=false\n" +
                "true=true\n" +
                "False=False \n" +
                "=\n" +
            "true=true\n" +
            "true=true\n" +
            "true=true", result("testif.jam"));
    }

    @Test
    @DisplayName("arguments are not replaced when present in the returned text of evaluated macros because of text segment splitting")
    void deepArgRef() throws BadSyntax, IOException {
        assertEquals("XX.. well, X is simply three aaa", result("deep_arg_ref.jam"));
    }

    @Test
    @DisplayName("All macro test that has .expected pair result what is expected")
    void testAllExcpected() throws IOException, BadSyntax {
        final var directoryName = fixupPath(this.getClass().getResource("").getFile());
        final var directory = new File(directoryName);
        int i = 0;
        for (final var expectedFile : directory.list((File dir, String name) -> name.endsWith(".expected"))) {
            final var inputFile = expectedFile.substring(0, expectedFile.length() - ".expected".length());
            assertEquals(expected(expectedFile), result(inputFile));
            i++;
        }
        // make sure all tests executed
        final int NUMBER_OF_TESTS = 4;
        assertFalse(NUMBER_OF_TESTS < i, "The number of tests is now different. You have new tests.");
        assertFalse(NUMBER_OF_TESTS > i, "Some of the tests were not executed or you deleted some test but not corrected the number of tests.");
    }

    private int getInt(Processor sut, String s) throws BadSyntax {
        return Integer.parseInt(((UserDefinedMacro) sut.getRegister().getUserDefined(s).get()).evaluate());
    }
    private String getString(Processor sut, String s) throws BadSyntax {
        return ((UserDefinedMacro) sut.getRegister().getUserDefined(s).get()).evaluate();
    }
    @Test
    @DisplayName("All macro test that has .expected pair result what is expected")
    void testAllFailure() throws IOException, BadSyntax {
        final var directoryName = fixupPath(this.getClass().getResource("").getFile());
        final var directory = new File(directoryName);
        int i = 0;
        for (final var errFile : directory.list((File dir, String name) -> name.endsWith(".err"))) {
            var in = createInput(errFile);
            final var sut = new Processor("{", "}");
            final var thrown = assertThrows(BadSyntaxAt.class, () -> sut.process(in));
            final var line = getInt(sut,"line");
            final var column = getInt(sut,"column");
            final var msg = getString(sut,"message");
            Assertions.assertEquals(line, thrown.getPosition().line);
            Assertions.assertEquals(column, thrown.getPosition().column);
            Assertions.assertTrue(thrown.getMessage().startsWith("Macro evaluated result user defined macro name contains the separator. Must not."));
            i++;
        }
        // make sure all tests executed
        final int NUMBER_OF_TESTS = 2;
        assertFalse(NUMBER_OF_TESTS < i, "The number of tests is now different. You have new tests.");
        assertFalse(NUMBER_OF_TESTS > i, "Some of the tests were not executed or you deleted some test but not corrected the number of tests.");
    }

    @Test
    void testErrorLineReport() {
        final var thrown = assertThrows(BadSyntaxAt.class, () -> result("fail.deep.jam"));
        Assertions.assertEquals(3, thrown.getPosition().line);
        Assertions.assertEquals(5, thrown.getPosition().column);
    }

}
