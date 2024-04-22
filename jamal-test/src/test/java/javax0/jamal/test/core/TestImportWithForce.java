package javax0.jamal.test.core;

import javax0.jamal.DocumentConverter;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestImportWithForce {

    @Test
    void test() throws Exception {
        final var root = DocumentConverter.getRoot();
        TestThat.theInput("{%@import included-no-force.jim%}->{%?A%}<-empty\n" +
                        "{%@import included.jim%}->{%A%}<-not empty").usingTheSeparators("{%", "%}")
                .atPosition(root + "/jamal-test/TEST_PLAYGROUND/SUBDIRECTORY/SUBDIRECTORY/test.jam", 0, 0)
                .results("-><-empty\n" +
                        "->A is defined<-not empty");
    }
}
