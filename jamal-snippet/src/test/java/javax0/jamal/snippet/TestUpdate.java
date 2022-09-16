package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class TestUpdate {
    @Test
    void testUpdate() throws Exception {
        final var file = "target/document_update_test.jam";
        try (final var output = new FileOutputStream(file)) {
            output.write(
                ("{@snip:define a=this is the snippet a}\n" +
                    "{@snip a\n" +
                    "}\n" +
                    "\n" +
                    "{#snip:update\n" +
                    "{@define head=[source]\n" +
                    "----\n" +
                    "}{@define tail=----\n" +
                    "}}").getBytes(StandardCharsets.UTF_8));
        }
        TestThat.theInput("{@include " + file + "}").results("\n" +
            "this is the snippet a\n" +
            "\n");
        try (final var lines = Files.lines(Paths.get(file))) {
            final var result = lines.collect(Collectors.joining("\n"));
            Assertions.assertEquals("{@snip:define a=this is the snippet a}\n" +
                "{@snip a\n" +
                "[source]\n" +
                "----\n" +
                "this is the snippet a\n" +
                "----\n" +
                "}\n" +
                "\n" +
                "{#snip:update\n" +
                "{@define head=[source]\n" +
                "----\n" +
                "}{@define tail=----\n" +
                "}}", result);
        }
    }


    @Test
    void testUpdateWithOldContent() throws Exception {
        final var file = "target/document_update_test.jam";
        try (final var output = new FileOutputStream(file)) {
            output.write(
                ("{@snip:define a=this is the snippet a}\n" +
                    "{@snip a\n" +
                    "this is the old content\n" +
                    "}\n" +
                    "\n" +
                    "{#snip:update\n" +
                    "{@define head=[source]\n" +
                    "----\n" +
                    "}{@define tail=----\n" +
                    "}}").getBytes(StandardCharsets.UTF_8));
        }
        TestThat.theInput("{@include " + file + "}").results("\n" +
            "this is the snippet a\n" +
            "\n");
        try (final var lines = Files.lines(Paths.get(file))) {
            final var result = lines.collect(Collectors.joining("\n"));
            Assertions.assertEquals("{@snip:define a=this is the snippet a}\n" +
                "{@snip a\n" +
                "[source]\n" +
                "----\n" +
                "this is the snippet a\n" +
                "----\n" +
                "}\n" +
                "\n" +
                "{#snip:update\n" +
                "{@define head=[source]\n" +
                "----\n" +
                "}{@define tail=----\n" +
                "}}", result);
        }
    }

    @Test
    void testUnterminated() throws Exception {
        final var file = "target/document_update_test.jam";
        try (final var output = new FileOutputStream(file)) {
            output.write(
                ("{@snip:define a=this is the snippet a}\n" +
                    "{@snip a\n}wups\n" +
                    "\n" +
                    "{#snip:update\n" +
                    "{@define head=[source]\n" +
                    "----\n" +
                    "}{@define tail=----\n" +
                    "}}").getBytes(StandardCharsets.UTF_8));
        }
        TestThat.theInput("{@include " + file + "}").throwsBadSyntax();
    }

    @Test
    void unnamed()throws Exception{
        TestThat.theInput("{@snip:update}").throwsBadSyntax("Cannot invoke update from an environment that has no file name");
    }

}
