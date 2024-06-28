package javax0.jamal.test;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.Processor;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/*
{%@snip:xml V=https://repo.maven.apache.org/maven2/com/javax0/jamal/jamal-api/maven-metadata.xml%}
{%#define RELEASE={%V //metadata/versioning/release%}%}
{%@import ../../../../../../../version.jim%}
{%@do
if LAST_RELEASE != RELEASE then
<<! "{%#error LAST_RELEASE is {%LAST_RELEASE%} when it should be {%RELEASE%}%}"
endif%}

 */

/**
 * Test that the macro {@code LAST_RELEASE} is the same as the one in the maven repo.
 * <p>
 * Do this only for SNAPSHOT versions.
 * It would make the build unreproducible for release versions.
 * (Not that it is so stable now.)
 * <p>
 * The source code of this Java class is processed by Jamal, and it will fail if the macro is different from the maven repo.
 */
public class TestLastRelease {

    @Test
    public void testLastRelease() throws Exception {
        if (Processor.jamalVersionString().endsWith("SNAPSHOT")) {
            final var me = DocumentConverter.getRoot() + "/jamal-test/src/test/java/javax0/jamal/test/TestLastRelease.";
            DocumentConverter.convert(me + "java");
            Files.delete(Path.of(me));
        }
    }

}
