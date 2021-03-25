package javax0.jamal.cmd;

import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCreateStarters {

    /**
     * Find the project root directory based on the fact that this is the only directory that has a {@code ROOT.dir}
     * file. Start with the current working directory and works up maximum 10 directories.
     *
     * @return the canonical path of the project root directory
     * @throws IOException if the directory structure cannot be read
     */
    static String getDirectory() throws IOException {
        var rootDir = new StringBuilder("ROOT.dir");
        var counter = new AtomicInteger(10);
        File rootDirFile;
        while (true) {
            rootDirFile = new File(rootDir.toString());
            if (rootDirFile.exists()) {
                break;
            }
            rootDir.insert(0, "../");
            Assertions.assertTrue(counter.decrementAndGet() > 0,
                "Cannot find the ROOT.dir file.");
        }
        final var parent = rootDirFile.getParentFile();
        return parent == null ? "." : parent.getCanonicalPath();
    }

    @Test
    void testCreateTheJBangStarterFile() throws Exception {
        final var content = TestThat.theInput("{@include res:jbang.template}").results();
        final var root = getDirectory();
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
        final var content = TestThat.theInput("{@include res:jamal.sh.template}").results();
        final var root = getDirectory();
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
        final var root = getDirectory();
        final var saveOut = System.out;
        final var testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
        JamalMain.main(new String[]{"-h"});
        System.setOut(saveOut);
        Files.write(Paths.get(root + "/" + "usage.txt"), testOut.toByteArray(),
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
