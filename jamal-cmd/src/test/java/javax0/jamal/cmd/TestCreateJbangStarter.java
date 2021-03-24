package javax0.jamal.cmd;

import javax0.jamal.api.Processor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCreateJbangStarter {

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
    void testCreateTheJBangStarterFile() throws IOException {
        final var root = getDirectory();
        final var version = new Properties();
        Processor.jamalVersion(version);
        final var versionString = version.getProperty("version");
        final var content = new StringBuilder("///usr/bin/env jbang \"$0\" \"$@\" ; exit $?\n");
        content.append("//JAVA 11+\n");
        for (final var module : List.of("engine", "api", "tools", "core", "cmd", "snippet", "scriptbasic",
            "groovy", "ruby", "plantuml", "debug")) {
            content.append("//DEPS com.javax0.jamal:jamal-").append(module).append(":").append(versionString).append("\n");
        }
        content.append("\n");
        content.append("import javax0.jamal.cmd.JamalMain;\n");
        content.append("\n");
        content.append("class jbangstarter {\n");
        content.append("    public static void main(String... args) {\n");
        content.append("        JamalMain.main(args);\n");
        content.append("    }\n");
        content.append("}");

        if (!versionString.contains("-")) {
            Files.write(Paths.get(root + "/" + "jbangstarter.java"), content.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
        Files.write(Paths.get(root + "/" + "jbangstarter." + versionString + ".java"), content.toString().getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

    }
}
