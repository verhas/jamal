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
        final var actual = process("abrakadabra\n" +
                "{%zebra%}");
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
        process("abrakadabra\n{%zebra%}");
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
         * 0 = "2022-11-25T18:41:50.808224 [0:1:main:0BAE47A0] started"
         * 1 = "2022-11-25T18:41:50.850251 [0:1:main:0BAE47A0] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         * 2 = "2022-11-25T18:41:52.107846 [0:1:main:0BAE47A0] saved"
         * 3 = "2022-11-25T18:41:52.110372 [0:1:main:0BAE47A0] dependencies"
         * 4 = "  c7ab6b1a6410dbb9a993a50496a8578d.adoc.jim vpf/bqeUngN5AtDqUrH5Rw=="
         * 5 = ""
         * 6 = "2022-11-25T18:41:52.112320 [0:1:main:0BAE47A0] not adding ludes"
         * 7 = "2022-11-25T18:41:52.113987 [0:1:main:0BAE47A0] setting cache"
         * 8 = "2022-11-25T18:41:52.115917 [0:1:main:0BAE47A0] DONE"
         * 9 = "2022-11-25T18:41:52.123638 [1:1:main:00D02F8D] started"
         * 10 = "2022-11-25T18:41:52.128682 [1:1:main:00D02F8D] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         * 11 = "2022-11-25T18:41:52.152902 [1:1:main:00D02F8D] restored"
         * 12 = "2022-11-25T18:41:52.160458 [1:1:main:00D02F8D] saved"
         * 13 = "2022-11-25T18:41:52.161605 [1:1:main:00D02F8D] dependencies"
         * 14 = "  c7ab6b1a6410dbb9a993a50496a8578d.adoc.jim vpf/bqeUngN5AtDqUrH5Rw=="
         * 15 = ""
         * 16 = "2022-11-25T18:41:52.163556 [1:1:main:00D02F8D] not adding ludes"
         * 17 = "2022-11-25T18:41:52.164491 [1:1:main:00D02F8D] setting cache"
         * 18 = "2022-11-25T18:41:52.165938 [1:1:main:00D02F8D] DONE"
         * 19 = "2022-11-25T18:41:52.167953 [2:1:main:45F24169] started"
         * 20 = "2022-11-25T18:41:52.169558 [2:1:main:45F24169] md5 JhxgLaP1ca4+e7vC8mNK7Q=="
         * 21 = "2022-11-25T18:41:52.201478 [2:1:main:45F24169] saved"
         * 22 = "2022-11-25T18:41:52.203108 [2:1:main:45F24169] dependencies"
         * 23 = "  c7ab6b1a6410dbb9a993a50496a8578d.adoc.jim oEZhG+ixKcULiztw1AYivQ=="
         * 24 = ""
         * 25 = "2022-11-25T18:41:52.204118 [2:1:main:45F24169] not adding ludes"
         * 26 = "2022-11-25T18:41:52.206446 [2:1:main:45F24169] setting cache"
         * 27 = "2022-11-25T18:41:52.208654 [2:1:main:45F24169] DONE"
         */
        final var lines = Files.readAllLines(Paths.get(LOG_OUTPUT), StandardCharsets.UTF_8);
        Assertions.assertTrue(lines.get(11).endsWith("restored"));
        Assertions.assertEquals("  " + TEST_IMPORT + " vpf/bqeUngN5AtDqUrH5Rw==", lines.get(14));
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
        Mockito.when(reader.readLines()).thenReturn(List.of(input.split("\n")));
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
