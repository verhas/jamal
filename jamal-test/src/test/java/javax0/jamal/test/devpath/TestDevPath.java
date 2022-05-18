package javax0.jamal.test.devpath;

import javax0.jamal.DocumentConverter;
import javax0.jamal.test.tools.junit.IntelliJOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestDevPath {

    /**
     * This test checks that the dev path is read properly form a file.
     *
     * If you run this test from Maven surefire then it will fail. The reason for that is simple. The dev path is
     * read when the FileTools class is loaded. When you run it from IntelliJ it starts new. When you start from
     * Maven it was already loaded and does not re-read the system property or environment variable.
     *
     * @throws Exception
     */
    @Test
    @IntelliJOnly
    void testDevPath() throws Exception {
        // GIVEN
        final var root = DocumentConverter.getRoot() + "/jamal-test/src/test/resources/devpath";

        System.setProperty("jamal.dev.path", root + "/devre.txt");

        Files.writeString(Paths.get(root + "/devre.txt"),
                "" +
                        "\n# this is a comment line" +
                        "       \n" // this is an empty line
                        + root + "/devre.in=" + root + "/devre.re\n\n");


        Files.writeString(Paths.get(root + "/devre.out.jam"),
                "" +
                        "pref\n" +
                        "\n" +
                        "{%@include " + root + "/devre.in%}\n" +
                        "\n" +
                        "postf");

        // WHEN
        DocumentConverter.convert(root + "/devre.out.jam");

        //THEN
        Assertions.assertEquals("pref\n" +
                "\n" +
                "THIS ONE\n" +
                "\n" +
                "postf", Files.readString(Paths.get(root + "/devre.out"), StandardCharsets.UTF_8));

        // SHOWDOWN
        System.clearProperty("jamal.dev.path");
    }
}
