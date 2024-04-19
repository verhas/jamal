package javax0.jamal.test.core;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestBug20240417 {

    @Test
    void test() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{%@import included.jim%}{%A%}").usingTheSeparators("{%", "%}")
                .atPosition(root + "/jamal-test/TEST_PLAYGROUND/SUBDIRECTORY/SUBDIRECTORY/test.jam", 0, 0)
                .results();
    }
}
