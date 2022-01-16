package javax0.jamal.cmd;

import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import static javax0.jamal.DocumentConverter.getRoot;

public class TestCreateStarters {

    @Test
    void testCreateTheJBangStarterFile() throws Exception {
        final var root = getRoot();
        final var content = TestThat.theInput("{@include res:jbang.template}").define("ROOT", root).results();
        final var version = new Properties();
        Processor.jamalVersion(version);
        final var versionString = version.getProperty("version");

        if (!versionString.contains("-")) {
            Files.write(Paths.get(root + "/" + "jbangstarter.java"), content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
        Files.write(Paths.get(root + "/" + "jbangstarter." + versionString + ".java"), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @Test
    void testCreateJamalSh() throws Exception {
        final var root = getRoot();
        final var content = TestThat.theInput("{@include res:jamal.sh.template}").define("ROOT", root).results();
        final var version = new Properties();
        Processor.jamalVersion(version);
        final var versionString = version.getProperty("version");

        Files.write(Paths.get(root + "/" + "jamal.sh"), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Files.write(Paths.get(root + "/" + "jamal." + versionString + ".sh"), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @Test
    void testCreateUsageSnippetForDocumentation() throws Exception {
        final var root = getRoot();
        final var saveOut = System.out;
        final var testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
        JamalMain.main(new String[]{"-h"});
        System.setOut(saveOut);
        Files.write(Paths.get(root + "/" + "usage.txt"), testOut.toByteArray(),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
