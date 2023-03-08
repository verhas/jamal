package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestBase64 {

    @DisplayName("Test text can be encoded using base64")
    @Test
    public void testEncode() throws Exception {
        TestThat.theInput("{@base64:encode Hello World!}").results("SGVsbG8gV29ybGQh");
    }

    @DisplayName("Test quoted text can be encoded using base64")
    @Test
    public void testEncodeQuoted() throws Exception {
        TestThat.theInput("{@base64:encode (quote) 'Hello World!'}").results("SGVsbG8gV29ybGQh");
    }

    @DisplayName("Different opening and closing quite will throw an exception")
    @Test
    public void testEncodeQuotedWrong() throws Exception {
        TestThat.theInput("{@base64:encode (quote) \"Hello World!'}").throwsBadSyntax("The text to be encoded must be quoted with the same character\\.");
    }

    @DisplayName("Text can be decoded using base64")
    @Test
    public void testDecode() throws Exception {
        TestThat.theInput("{@base64:decode IEhlbGxvIFdvcmxkIQ==}").results(" Hello World!");
    }

    @DisplayName("Quoted text can be decoded using base64")
    @Test
    public void testDecodeQuoted() throws Exception {
        TestThat.theInput("{@base64:decode (quote) \"IEhlbGxvIFdvcmxkIQ==\"}").results(" Hello World!");
    }

    @DisplayName("Wrong base64 string will throw an exception")
    @Test
    public void testDecodeWrong() throws Exception {
        TestThat.theInput("{@base64:decode Hello World!}").throwsBadSyntax("Illegal base64 string");
    }

}
