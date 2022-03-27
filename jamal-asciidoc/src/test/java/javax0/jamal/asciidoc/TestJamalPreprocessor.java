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
import java.util.List;

public class TestJamalPreprocessor {
    private static final String TEST_OUTPUT = "c7ab6b1a6410dbb9a993a50496a8578d.adoc";
    private static final String TEST_INPUT = TEST_OUTPUT + ".jam";
    private static final String LOG_OUTPUT = TEST_OUTPUT + ".log";

    @BeforeEach // better safe than sorry
    @AfterEach
    void clean() {
        //noinspection ResultOfMethodCallIgnored
        new File(TEST_OUTPUT).delete();
        //noinspection ResultOfMethodCallIgnored
        new File(LOG_OUTPUT).delete();
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
                        "* User defined macro '{%zebra ...' is not defined. at " + TEST_INPUT + "/1:3\n" +
                        "--\n" +
                        "abrakadabra\n" +
                        "[WARNING]\n" +
                        "--\n" +
                        "* User defined macro '{%zebra ...' is not defined. at " + TEST_INPUT + "/1:3\n" +
                        "--\n" +
                        "{%zebra%}\n" +
                        "[WARNING]\n" +
                        "--\n" +
                        "* User defined macro '{%zebra ...' is not defined. at " + TEST_INPUT + "/1:3\n" +
                        "--\n" +
                        "[source]\n" +
                        "----\n" +
                        "User defined macro '{%zebra ...' is not defined. at " + TEST_INPUT + "/1:3\n" +
                        "javax0.jamal.api.BadSyntaxAt: User defined macro '{%zebra ...' is not defined. at " + TEST_INPUT + "/1:3\n", actual);
    }

    private static void assertStartsWith(final String expected, final String actual) {
        Assertions.assertEquals(expected, actual.replaceAll("\r\n","\n").substring(0, Math.min(expected.length(), actual.length())));
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

    private String process(String input) {
        return process(input, null);
    }

    private String process(String input, String expected) {
        final var sut = new JamalPreprocessor();
        Document document = null;
        PreprocessorReader reader = Mockito.mock(PreprocessorReader.class);
        Mockito.when(reader.getFile()).thenReturn(TEST_INPUT);
        Mockito.when(reader.readLines()).thenReturn(List.of(input.split("\n")));
        final var response = ArgumentCaptor.forClass(List.class);
        sut.process(document, reader);
        Mockito.verify(reader).restoreLines(response.capture());
        final var actual = String.join("\n", response.getValue());
        if (expected != null) {
            Assertions.assertEquals(expected, actual);
        }
        return actual;
    }
}
