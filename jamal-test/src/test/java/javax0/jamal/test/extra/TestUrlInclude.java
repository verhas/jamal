package javax0.jamal.test.extra;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A simple test that shows that web pages can be imported.
 */
public class TestUrlInclude {

    @DisplayName("Test that a web page can also be imported")
    @Test
    void test() throws Exception {
        TestThat.theInput("{@import https://raw.githubusercontent.com/jamalrepo/pom/main/plugins/compiler.jim}").results("");
    }
}
