package javax0.jamal.asciidoc;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.PreprocessorReader;
import org.junit.jupiter.api.*;
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
    private static final String SEGMENT = "[zebra]/1:3\n";

    @BeforeEach // better safe, than sorry
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
                        "* User macro '{%zebra ...' is not defined. Did you mean '@debug'? at " + TEST_INPUT + SEGMENT +
                        "--\n" +
                        "abrakadabra\n" +
                        "[WARNING]\n" +
                        "--\n" +
                        "* User macro '{%zebra ...' is not defined. Did you mean '@debug'? at " + TEST_INPUT + SEGMENT +
                        "--\n" +
                        "{%zebra%}\n" +
                        "[WARNING]\n" +
                        "--\n" +
                        "* User macro '{%zebra ...' is not defined. Did you mean '@debug'? at " + TEST_INPUT + SEGMENT +
                        "--\n" +
                        "[source]\n" +
                        "----\n" +
                        "User macro '{%zebra ...' is not defined. Did you mean '@debug'? at " + TEST_INPUT + SEGMENT
                , actual);
    }


    @Test
    @DisplayName("Output is created even when there is an error")
    void testOutputInCaseOfError() {
        process("abrakadabra\n{%not_defined%}");
        Assertions.assertTrue(new File(TEST_OUTPUT).exists());
    }

    @Test
    @DisplayName("Output file is created when no error")
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
         * 0 = "2022-12-09T09:09:26.769836 [0:1:main:0A0A9FA5] started"
         * 1 = "2022-12-09T09:09:26.789528 [0:1:main:0A0A9FA5] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         * 2 = "2022-12-09T09:09:26.955943 [0:1:main:0A0A9FA5] setting cache"
         * 3 = "2022-12-09T09:09:26.957778 [0:1:main:0A0A9FA5] saved"
         * 4 = "2022-12-09T09:09:26.961728 [0:1:main:0A0A9FA5] dependencies"
         * 5 = "  c7ab6b1a6410dbb9a993a50496a8578d.adoc.jim vpf/bqeUngN5AtDqUrH5Rw=="
         * 6 = ""
         * 7 = "2022-12-09T09:09:26.962332 [0:1:main:0A0A9FA5] not adding prelude and post lude, it is an asccidoc file"
         * 8 = "2022-12-09T09:09:26.964700 [0:1:main:0A0A9FA5] Keeping the front matter, or no front matter"
         * 9 = "2022-12-09T09:09:26.965497 [0:1:main:0A0A9FA5] DONE"
         * 10 = "2022-12-09T09:09:26.974314 [1:1:main:352E787A] started"
         * 11 = "2022-12-09T09:09:26.975508 [1:1:main:352E787A] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         * 12 = "2022-12-09T09:09:26.984029 [1:1:main:352E787A] restored"
         * 13 = "2022-12-09T09:09:26.984484 [1:1:main:352E787A] not adding prelude and post lude, it is an asccidoc file"
         * 14 = "2022-12-09T09:09:26.986272 [1:1:main:352E787A] Keeping the front matter, or no front matter"
         * 15 = "2022-12-09T09:09:26.986746 [1:1:main:352E787A] DONE"
         * 16 = "2022-12-09T09:09:26.989539 [2:1:main:7FC645E4] started"
         * 17 = "2022-12-09T09:09:26.990004 [2:1:main:7FC645E4] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         * 18 = "2022-12-09T09:09:27.011614 [2:1:main:7FC645E4] setting cache"
         * 19 = "2022-12-09T09:09:27.014887 [2:1:main:7FC645E4] saved"
         * 20 = "2022-12-09T09:09:27.016931 [2:1:main:7FC645E4] dependencies"
         * 21 = "  c7ab6b1a6410dbb9a993a50496a8578d.adoc.jim oEZhG+ixKcULiztw1AYivQ=="
         * 22 = ""
         * 23 = "2022-12-09T09:09:27.017441 [2:1:main:7FC645E4] not adding prelude and post lude, it is an asccidoc file"
         * 24 = "2022-12-09T09:09:27.019282 [2:1:main:7FC645E4] Keeping the front matter, or no front matter"
         * 25 = "2022-12-09T09:09:27.019865 [2:1:main:7FC645E4] DONE"
         */
        final var lines = Files.readAllLines(Paths.get(LOG_OUTPUT), StandardCharsets.UTF_8);
        Assertions.assertEquals("  " + TEST_IMPORT + " vpf/bqeUngN5AtDqUrH5Rw==", lines.get(5));
        Assertions.assertTrue(lines.get(12).endsWith("restored"));
        Assertions.assertFalse(lines.get(19).endsWith("restored"));
    }

    private static void assertStartsWith(final String expected, final String actual) {
        Assertions.assertEquals(expected, actual.replaceAll("\r\n", "\n").substring(0, Math.min(expected.length(), actual.length())));
    }

    private String process(String input) {
        return process(input, null);
    }

    private String process(String input, String expected) {
        final var sut = new JamalPreprocessor();
        Document document = Mockito.mock(Document.class);
        Mockito.when(document.getAttribute("front-matter", "")).thenReturn("");
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
