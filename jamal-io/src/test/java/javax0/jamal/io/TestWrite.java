package javax0.jamal.io;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestWrite {

    @Test
    void testSimpleWrite() throws Exception {
        TestThat.theInput("" +
            "{@define fileName=./target/wups/file.txt}" +
            "{#io:write (file={fileName} mkdir)" +
            "this is the text into the\n" +
            "file\n" +
            "}{#include [verbatim] {fileName}}" +
            "{#io:remove recursive file=./target/wups}"+
            "{@io:print (err)I'm done}"
        ).results("" +
            "this is the text into the\n" +
            "file"
        );
    }
}
