package javax0.jamal.test.core;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestEvalistFor {
    @Test
    void testEvalistInclude() throws Exception {
        DocumentConverter.convert(DocumentConverter.getRoot() + "/jamal-test/src/test/resources/import/evalistfor.jam");
        Assertions.assertEquals("* 01\n" +
                        "* 02\n" +
                        "* 03\n" +
                        "* 04\n" +
                        "* 05\n" +
                        "* 06\n" +
                        "* 07\n" +
                        "* 08\n" +
                        "* 09\n" +
                        "* 10\n" +
                        "* 11\n" +
                        "* 12\n" +
                        "* 13\n" +
                        "* 14\n" +
                        "* 15\n" +
                        "* 16\n" +
                        "* 17\n" +
                        "* 18\n" +
                        "* 19\n" +
                        "\n" +
                        "\n",
        new String(Files.readAllBytes(Paths.get(DocumentConverter.getRoot() + "/jamal-test/src/test/resources/import/evalistfor"))));
        new File(DocumentConverter.getRoot() + "/jamal-test/src/test/resources/import/evalistfor").delete();
    }
}
