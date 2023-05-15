package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestJavaSourceInsert {


    @Test
    void testSimpleInsert() throws BadSyntax, Exception {
        String testFile = "target/Test.java";
        Path path = Paths.get(testFile);
        Files.write(path, ("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"this is the id\">\n" +
                "This text will be deleted and replaced with the generated code\n" +
                "//</editor-fold>\n" +
                "").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        TestThat.theInput("This is the text that will get inserted\n" +
                "{@java:insert to=\"" + testFile + "\" id=\"this is the id\"}\n" +
                "").results("This is the text that will get inserted\n\n");
        final var result = Files.readString(path);
        Assertions.assertEquals("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"this is the id\">\n" +
                "This is the text that will get inserted\n" +
                "\n" +
                "\n" +
                "//</editor-fold>\n", result);
    }

    @Test
    void testSimpleUpdateOnly() throws BadSyntax, Exception {
        String testFile = "target/Test.java";
        Path path = Paths.get(testFile);
        Files.write(path, ("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"this is the id\">\n" +
                "This is the text that will get inserted\n" +
                "//</editor-fold>\n" +
                "").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        // spaces will not be considered when update only
        TestThat.theInput("This is the text     that    will      get inserted\n" +
                "{@java:insert update to=\"" + testFile + "\" id=\"this is the id\"}\n" +
                "").results("This is the text     that    will      get inserted\n" +
                "\n");
        final var result = Files.readString(path);
        Assertions.assertEquals("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"this is the id\">\n" +
                "This is the text that will get inserted\n" +
                "//</editor-fold>\n", result);
    }

}
