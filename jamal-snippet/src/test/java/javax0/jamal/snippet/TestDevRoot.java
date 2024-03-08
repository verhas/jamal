package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static javax0.jamal.DocumentConverter.getRoot;

/**
 *
 */
public class TestDevRoot {

    @Test
    @DisplayName("find the root of the Jamal project")
    void testLocateFindRoot() throws Exception {
        final var root = getRoot();
        final var s = TestThat.theInput("{@dev:root format=$absolutePath}")
                .atPosition(root + "/jamal-snippet/src/test/resources/reldirt1/z.txt", 1, 1)
                .results();
        Assertions.assertTrue(new File(s + "/versions.adoc.jam").exists());
    }

}
