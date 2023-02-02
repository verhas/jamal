package javax0.jamal.maven.input;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestMavenInput {

    @DisplayName("MavenInput says it can read something that starts with 'maven:")
    @Test
    void testCanRead() {
        final var sut = new MavenInput();
        Assertions.assertTrue(sut.canRead("maven:groupId:artifactId:version:classifier:fileName"));
    }

    @DisplayName("MavenInput says it cannot read something that does not start with 'maven:")
    @Test
    void testCantRead() {
        final var sut = new MavenInput();
        Assertions.assertFalse(sut.canRead("mivan:?groupId:artifactId:version:classifier:fileName"));
    }

    @DisplayName("Downloads and fetches file from maven repo")
    @Test
    void canDownloadAndRead() throws Exception {
        final var sut = new MavenInput();
        final var content = sut.read("maven:com.javax0.jamal:jamal-api:1.12.5::META-INF/MANIFEST.MF");
        Assertions.assertEquals("Manifest-Version: 1.0\r\n" +
                "Archiver-Version: Plexus Archiver\r\n" +
                "Created-By: Apache Maven 3.8.4\r\n" +
                "Built-By: verhasp\r\n" +
                "Build-Jdk: 17.0.2\r\n" +
                "\r\n", content);
    }

    @DisplayName("Downloads and fetches file from maven repo recursively")
    @Test
    void canDownloadAndReadRecursively() throws Exception {
        final var sut = new MavenInput();
        final var content = sut.read("maven:com.javax0.jamal:jamal-api:1.12.5::META-INF/MANIFEST.MF");
        Assertions.assertEquals("Manifest-Version: 1.0\r\n" +
                "Archiver-Version: Plexus Archiver\r\n" +
                "Created-By: Apache Maven 3.8.4\r\n" +
                "Built-By: verhasp\r\n" +
                "Build-Jdk: 17.0.2\r\n" +
                "\r\n", content);
    }

    @DisplayName("Downloads and fetches file from maven repo recursively")
    @Test
    void canDownloadAndRead1() throws Exception {
        final var sut = new MavenInput();
        final var content = sut.read("maven:com.javax0.jamal:jamal-groovy:1.12.5::groovy.jim");
        Assertions.assertEquals("{@comment define macros that help the use of the built-in groovy macros}\n" +
                "\n" +
                "{@comment\n" +
                "\n" +
                "You can use this macro to declare the name of the groovy shell in case there are multiple shells used in the\n" +
                "Jamal file. One possibility is to [@define groovyShell= name ]\n" +
                "\n" +
                "The other one is to use this macro and say [shell=name]\n" +
                "\n" +
                "}\n" +
                "{@define shell(x)={@define groovyShell=x}}", content);
    }

    @DisplayName("Do an import using a maven resource following dependency...")
    @Test
    void testViaMacros() throws Exception {
        TestThat.theInput("{@include [verbatim] maven:com.javax0.jamal:jamal-groovy:1.12.5:compile:version.properties}")
                .results("version=1.12.5");
    }
}
