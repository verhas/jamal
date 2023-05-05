package javax0.jamal.snippet;

import javax0.jamal.api.Position;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestJavaInsert {


    public static final String TARGET_TEST_JAVA_INSERT_JAVA = "target/TestJavaInsert.java";

    @Test
    void testJavaInsert() throws Exception{
        Files.write(Paths.get(TARGET_TEST_JAVA_INSERT_JAVA), ("public class TestJavaInsert {\n" +
                        "//<editor-fold >\n" +
                        "//</editor-fold>\n" +
                        "}\n" +
                        "").getBytes());
        TestThat.theInput("this is just a normal text\n" +
                "{@java:insert to=TestJavaInsert.java}\n"
        ).atPosition(new Position("target/test.jam"))
                .results();
        final var result = Files.readString(Paths.get(TARGET_TEST_JAVA_INSERT_JAVA));
        Assertions.assertEquals("public class TestJavaInsert {\n" +
                "//<editor-fold >\n" +
                "this is just a normal text\n" +
                "\n" +
                "\n" +
                "//</editor-fold>\n" +
                "}\n", result);
    }

}
