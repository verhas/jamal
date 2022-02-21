package javax0.jamal.io;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class TestWrite {

    @Test
    void testSimpleWrite() throws Exception {
        final var saveErr = System.err;
        final var out = new ByteArrayOutputStream();
        final var newErr = new PrintStream(out);
        System.setErr(newErr);
        TestThat.theInput("" +
                "{@define fileName=./target/wups/file.txt}" +
                "{#io:write (file={fileName} mkdir)" +
                "this is the text into the\n" +
                "file\n" +
                "}{#include [verbatim] {fileName}}" +
                "{#io:remove recursive file=./target/wups}" +
                "{@io:print (err)I'm done}")
            .ignoreLineEnding()
            .results("" +
                "this is the text into the\n" +
                "file\n"
            );
        newErr.close();
        out.close();
        Assertions.assertEquals("I'm done", out.toString(StandardCharsets.UTF_8));
        System.setErr(saveErr);
    }
}
