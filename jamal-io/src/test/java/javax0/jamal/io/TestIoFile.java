package javax0.jamal.io;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestIoFile {


    @Test
    @DisplayName("Test different tests with existing file")
    void testFile() throws Exception {
        TestThat.theInput("{@io:file file=src/test/resources/test.txt exists}").results("true");
        TestThat.theInput("{@io:file file=src/test/resources/test.txt isHidden}").results("false");
        TestThat.theInput("{@io:file file=src/test/resources/test.txt isFile}").results("true");
        TestThat.theInput("{@io:file file=src/test/resources/test.txt isDirectory}").results("false");
        TestThat.theInput("{@io:file file=src/test/resources/test.txt canRead}").results("true");
        TestThat.theInput("{@io:file file=src/test/resources/test.txt canWrite}").results("true");
        TestThat.theInput("{@io:file file=src/test/resources/test.txt canExecute}").results("false");
    }
    @Test
    @DisplayName("Test that only one option can be used")
    void testFileOneOnly() throws Exception {
        TestThat.theInput("{@io:file file=src/test/resources/test.txt exists isHidden}").throwsBadSyntax("The key 'isHidden' must not be multi valued in the macro 'io:file'");
    }
}
