package javax0.jamal.cmd;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import static javax0.jamal.DocumentConverter.getRoot;

public class TestCreateStarters {

    @Test
    @DisplayName("Create the jbang starter file from the resource jbang.template")
    void testCreateTheJBangStarterFile() throws Exception {
        final var versionString = getVersionString();
        final var root = getRoot();
        final var content = getContent("{@include res:jbang.template.jam}", root, versionString);

        if (!versionString.contains("-")) {
            Files.write(Paths.get(root + "/" + "jbangstarter.java"), content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
        Files.write(Paths.get(root + "/" + "jbangstarter." + versionString + ".java"), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @Test
    @DisplayName("Create the jamal.sh shell script using the dependencies plugin output and the resource jamal.sh.template")
    void testCreateJamalSh() throws Exception {
        final var versionString = getVersionString();
        final var root = getRoot();
        final var content = getContent("{@include res:jamal.sh.template.jam}", root, versionString);

        Files.write(Paths.get(root + "/" + "jamal.sh"), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Files.write(Paths.get(root + "/" + "jamal." + versionString + ".sh"), content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    private String getContent(final String input, final String root, final String versionString) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, BadSyntax {
        return TestThat.theInput(input)
                .define("ROOT", root)
                .results();
    }

    private String getVersionString() {
        final var version = new Properties();
        Processor.jamalVersion(version);
        return version.getProperty("version");
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
