package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUrlInclude {

    @DisplayName("Test that a web page can also be imported")
    @Test
    void test() throws Exception {
        //TestThat.theInput("{@sep /{[{/}]}}{[{@include https://raw.githubusercontent.com/verhas/jamal/master/README.md}]}").results("");
        //TestThat.theInput("{@import https://raw.githubusercontent.com/jamalrepo/pom/main/plugins/compiler.jim}").results("");
    }
}
