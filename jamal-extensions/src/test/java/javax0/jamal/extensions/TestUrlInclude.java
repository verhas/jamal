package javax0.jamal.extensions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestUrlInclude {

    @DisplayName("Test that a web page can also be imported")
    @Test
    void test() {
        //TestThat.theInput("{@sep /{[{/}]}}{[{@include https://raw.githubusercontent.com/verhas/jamal/master/README.md}]}").results("");
        // This is not a unit test and may fail in case there is no network connection
        //TestThat.theInput("{@import https://raw.githubusercontent.com/jamalrepo/pom/main/plugins/compiler.jim}").results("");
    }
}
