package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestJavaSourceInsert {


    @Test
    @DisplayName("Test that the snippet is inserted into the file")
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
    @DisplayName("Test that the snippet is not inserted into the file being the same as the one already there")
    void testSimpleUpdateOnly() throws Exception {
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

    @Test
    @DisplayName("Test that the snippet is not inserted into the file being the same as the one already there and thee is no error")
    void testSimpleUpdateOnlyNoError() throws Exception {
        String testFile = "target/Test.java";
        Path path = Paths.get(testFile);
        Files.write(path, ("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"this is the id\">\n" +
                "This is the text that will get inserted\n" +
                "//</editor-fold>\n" +
                "").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        // spaces will not be considered when update only
        TestThat.theInput("This is the text     that    will      get inserted\n" +
                "{@java:insert failOnUpdate to=\"" + testFile + "\" id=\"this is the id\"}\n" +
                "").results("This is the text     that    will      get inserted\n" +
                "\n");
        final var result = Files.readString(path);
        Assertions.assertEquals("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"this is the id\">\n" +
                "This is the text that will get inserted\n" +
                "//</editor-fold>\n", result);
    }
    @Test
    @DisplayName("Test that the snippet is inserted into the file being different from the one already there, and thee is an error")
    void testSimpleUpdateOnlyWithError() throws Exception {
        String testFile = "target/Test.java";
        Path path = Paths.get(testFile);
        Files.write(path, ("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"this is the id\">\n" +
                "Something that is totally different from what gets inserted\n" +
                "//</editor-fold>\n" +
                "").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        // this time the difference is not only spaces
        TestThat.theInput("This is the text that will get inserted" +
                "{@java:insert failOnUpdate to=\"" + testFile + "\" id=\"this is the id\"}\\\n" +
                "").throwsBadSyntax("The file target/Test.java was updated.");
        final var result = Files.readString(path);
        Assertions.assertEquals("This is some prelude text, not touched\n" +
                "//<editor-fold id=\"this is the id\">\n" +
                "This is the text that will get inserted\n" +
                "//</editor-fold>\n", result);
    }

    @Test
    @DisplayName("Test that multiple snippets are inserted into the file being different from the one already there, and thee is an error")
    void testMultipleUpdateOnlyWithError() throws Exception {
        String testFile = "target/Test.java";
        Path path = Paths.get(testFile);
        Files.write(path, ("This is some prelude text, not touched\n" +
                "//<editor-fold id=ID1>\n" +
                "Something that is totally different from what gets inserted\n" +
                "//</editor-fold>\n" +
                "//<editor-fold id=ID2>\n" +
                "Something total garbáž\n" +
                "//</editor-fold>\n" +
                "Some postlude text also to be on the safe side." +
                "").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        // this time the difference is not only spaces
        TestThat.theInput("" +
                "{@java:insert failOnUpdate to=\"" + testFile + "\" id=ID1\n" +
                "This is the text that will get inserted into the first segment}\\\n" +
                "{@java:insert failOnUpdate to=\"" + testFile + "\" id=ID2\n" +
                "This is the text that will get inserted into the second segment}\\\n" +
                "").throwsBadSyntax("The file target/Test.java was updated.");
        final var result = Files.readString(path);
        Assertions.assertEquals("This is some prelude text, not touched\n" +
                "//<editor-fold id=ID1>\n" +
                "This is the text that will get inserted into the first segment\n" +
                "//</editor-fold>\n" +
                "//<editor-fold id=ID2>\n" +
                "This is the text that will get inserted into the second segment\n" +
                "//</editor-fold>\n" +
                "Some postlude text also to be on the safe side.", result);
    }

}
