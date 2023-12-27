package javax0.jamal.jar.input;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestJarInput {

    @DisplayName("JarInput says it can read something that starts with 'jar:file:'")
    @Test
    void testCanRead() {
        final var sut = new JarInput();
        Assertions.assertTrue(sut.canRead("jar:file:///c:/Program Files/x86/Jamal/1.12.5/jamal-api-1.12.5.jar!/META-INF/MANIFEST.MF"));
    }

    @DisplayName("JarInput says it cannot read something that does not start with 'jar:file:'")
    @Test
    void testCantRead() {
        final var sut = new JarInput();
        Assertions.assertFalse(sut.canRead("jar:fele:///c:/Program Files/x86/Jamal/1.12.5/jamal-api-1.12.5.jar!/META-INF/MANIFEST.MF"));
    }

    @DisplayName("Opens a jar and reads from it")
    @Test
    void canOpenAndRead() throws Exception {
        final var sut = new JarInput();
        final var content = sut.read("jar:file:./src/test/resources/jamal-api-1.12.6.jar!META-INF/MANIFEST.MF");
        Assertions.assertEquals("Manifest-Version: 1.0\r\n" +
                "Archiver-Version: Plexus Archiver\r\n" +
                "Created-By: Apache Maven 3.8.4\r\n" +
                "Built-By: verhasp\r\n" +
                "Build-Jdk: 17.0.2\r\n" +
                "\r\n", content);
    }

    @DisplayName("Do an import using a maven resource following dependency...")
    @Test
    void testViaMacros() throws Exception {
        TestThat.theInput("{@include [verbatim] jar:file:./src/test/resources/jamal-api-1.12.6.jar!version.properties}")
                .results("version=1.12.6");
    }
}
