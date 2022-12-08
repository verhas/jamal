package javax0.jamal.asciidoc;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.PreprocessorReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestJamalPreprocessor {

    private static final String PRELUDE = "[source]\n----\n\n";
    private static final String POSTLUDE = "----\n";

    private static final String TEST_OUTPUT = "c7ab6b1a6410dbb9a993a50496a8578d.adoc";
    private static final String TEST_INPUT = TEST_OUTPUT + ".jam";
    private static final String TEST_IMPORT = TEST_OUTPUT + ".jim";
    private static final String LOG_OUTPUT = TEST_OUTPUT + ".log";

    @BeforeEach // better safe than sorry
    @AfterEach
    void clean() {
        // new File(TEST_INPUT).delete(); <-- it is not physically created, it is only a virtual name as a reference

        //noinspection ResultOfMethodCallIgnored
        new File(TEST_OUTPUT).delete();

        //noinspection ResultOfMethodCallIgnored
        new File(LOG_OUTPUT).delete();

        //noinspection ResultOfMethodCallIgnored
        new File(TEST_IMPORT).delete();
    }

    @Test
    @DisplayName("Test normal processing using {% and %}")
    void testDefault() {
        process("abrakadabra\n" +
                        "{%@define foo=bar%}{%foo%}",
                "abrakadabra\n" +
                        "bar");
    }

    @Test
    @DisplayName("Test normal processing using { and } when the text starts with {@")
    void testSimpleOpen() {
        process("{@comment}abrakadabra\n" +
                        "{@define foo=bar}{foo}",
                "abrakadabra\n" +
                        "bar");
    }


    @Test
    @DisplayName("Test normal switched off processing with { and }")
    void testOff1() {
        process("{@comment off}abrakadabra\n" +
                        "{@define foo=bar}{foo}",
                "{@comment off}abrakadabra\n" +
                        "{@define foo=bar}{foo}");
    }

    @Test
    @DisplayName("Test normal switched off processing with {% and %}")
    void testOff2() {
        process("{%@comment off%}abrakadabra\n" +
                        "{%@define foo=bar%}{%foo%}",
                "{%@comment off%}abrakadabra\n" +
                        "{%@define foo=bar%}{%foo%}");
    }

    @Test
    @DisplayName("Test erroneous input")
    void testError() {
        final var actual = process("abrakadabra\n{%zebra%}");
        assertStartsWith(
                "[WARNING]\n" +
                        "--\n" +
                        "* User defined macro '{%zebra ...' is not defined. Did you mean '@debug'? at " + TEST_INPUT + "/1:3\n" +
                        "--\n" +
                        "abrakadabra\n" +
                        "[WARNING]\n" +
                        "--\n" +
                        "* User defined macro '{%zebra ...' is not defined. Did you mean '@debug'? at " + TEST_INPUT + "/1:3\n" +
                        "--\n" +
                        "{%zebra%}\n" +
                        "[WARNING]\n" +
                        "--\n" +
                        "* User defined macro '{%zebra ...' is not defined. Did you mean '@debug'? at " + TEST_INPUT + "/1:3\n" +
                        "--\n" +
                        "[source]\n" +
                        "----\n" +
                        "User defined macro '{%zebra ...' is not defined. Did you mean '@debug'? at " + TEST_INPUT + "/1:3\n"
                , actual);
    }


    @Test
    @DisplayName("Output is created even when there is an error")
    void testOutputInCaseOfError() {
        process("abrakadabra\n{%not_defined%}");
        Assertions.assertTrue(new File(TEST_OUTPUT).exists());
    }

    @Test
    @DisplayName("Output file is created")
    void testNormalOutput() {
        process("abrakadabra\n{%@define foo=bar%}{%foo%}");
        Assertions.assertTrue(new File(TEST_OUTPUT).exists());
    }

    @Test
    @DisplayName("Test caching")
    void testCache() throws Exception {
        final Path testImportPath = Paths.get(TEST_IMPORT);
        Files.write(testImportPath, "{%@define foo=bar%}".getBytes(StandardCharsets.UTF_8));
        process("{%@comment log%}abrakadabra\n{%@import " + TEST_IMPORT + "%}{%foo%}", "abrakadabra\nbar");
        process("{%@comment log%}abrakadabra\n{%@import " + TEST_IMPORT + "%}{%foo%}", "abrakadabra\nbar");
        Files.write(testImportPath, "{%@define foo=barbar%}".getBytes(StandardCharsets.UTF_8));
        process("{%@comment log%}abrakadabra\n{%@import " + TEST_IMPORT + "%}{%foo%}", "abrakadabra\nbarbar");
        /*
         *  0 = "2022-12-08T11:16:27.295737 [0:1:main:085EC632] started"
         *  1 = "2022-12-08T11:16:27.315210 [0:1:main:085EC632] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         *  2 = "2022-12-08T11:16:27.455813 [0:1:main:085EC632] setting cache"
         *  3 = "2022-12-08T11:16:27.456721 [0:1:main:085EC632] saved"
         *  4 = "2022-12-08T11:16:27.458203 [0:1:main:085EC632] dependencies"
         *  5 = "  c7ab6b1a6410dbb9a993a50496a8578d.adoc.jim vpf/bqeUngN5AtDqUrH5Rw=="
         *  6 = ""
         *  7 = "2022-12-08T11:16:27.458521 [0:1:main:085EC632] not adding ludes"
         *  8 = "2022-12-08T11:16:27.460115 [0:1:main:085EC632] DONE"
         *  9 = "2022-12-08T11:16:27.465233 [1:1:main:061533AE] started"
         *  10 = "2022-12-08T11:16:27.468418 [1:1:main:061533AE] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         *  11 = "2022-12-08T11:16:27.473917 [1:1:main:061533AE] restored"
         *  12 = "2022-12-08T11:16:27.475250 [1:1:main:061533AE] not adding ludes"
         *  13 = "2022-12-08T11:16:27.475609 [1:1:main:061533AE] DONE"
         *  14 = "2022-12-08T11:16:27.477376 [2:1:main:4463D9D3] started"
         *  15 = "2022-12-08T11:16:27.477833 [2:1:main:4463D9D3] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         *  16 = "2022-12-08T11:16:27.492409 [2:1:main:4463D9D3] setting cache"
         *  17 = "2022-12-08T11:16:27.494772 [2:1:main:4463D9D3] saved"
         *  18 = "2022-12-08T11:16:27.495947 [2:1:main:4463D9D3] dependencies"
         *  19 = "  c7ab6b1a6410dbb9a993a50496a8578d.adoc.jim oEZhG+ixKcULiztw1AYivQ=="
         *  20 = ""
         *  21 = "2022-12-08T11:16:27.496240 [2:1:main:4463D9D3] not adding ludes"
         *  22 = "2022-12-08T11:16:27.497385 [2:1:main:4463D9D3] DONE"
         */
        final var lines = Files.readAllLines(Paths.get(LOG_OUTPUT), StandardCharsets.UTF_8);
        Assertions.assertTrue(lines.get(11).endsWith("restored"));
        Assertions.assertEquals("  " + TEST_IMPORT + " vpf/bqeUngN5AtDqUrH5Rw==", lines.get(5));
        Assertions.assertFalse(lines.get(18).endsWith("restored"));
    }

    private static void assertStartsWith(final String expected, final String actual) {
        Assertions.assertEquals(expected, actual.replaceAll("\r\n", "\n").substring(0, Math.min(expected.length(), actual.length())));
    }

    private String process(String input) {
        return process(input, null);
    }

    private String process(String input, String expected) {
        final var sut = new JamalPreprocessor();
        Document document = null;
        PreprocessorReader reader = Mockito.mock(PreprocessorReader.class);
        Mockito.when(reader.getFile()).thenReturn(TEST_INPUT);
        final var lines = List.of(input.split("\n"));
        Mockito.when(reader.readLines()).thenReturn(lines);
        final var response = (ArgumentCaptor<List<String>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        sut.process(document, reader);
        Mockito.verify(reader).restoreLines(response.capture());
        final var actual = String.join("\n", response.getValue());
        if (expected != null) {
            Assertions.assertEquals(expected, actual);
        }
        return actual;
    }
}
