package javax0.jamal.cmd;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.Processor;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestCreateVersionDependentPostInstallFiles {

    private static final String PACKAGING_RESOURCES = "src/packaging-resources/";
    String root;
    String version = Processor.jamalVersionString();

    private void deleteIfExists(final Path resource) {
        try {
            Files.delete(resource);
        } catch (Exception ignored) {
        }
    }

    private void write(final String resource, final String content) throws Exception {
        final var path = Path.of(root + PACKAGING_RESOURCES + resource);
        deleteIfExists(path);
        Files.writeString(path,
                content.replaceAll("\\$\\{VERSION\\}", version));
    }

    @Test
    void createVersionDependentPostInstallFiles() throws Exception {
        root = DocumentConverter.getRoot(".mvn") +"/jamal-cmd/";
        write("jamal",
                "#!/bin/sh\n" +
                        "INSTALL_DIR=\"$(dirname \"$(dirname \"$(readlink -f \"$0\")\")\")\"\n" +
                        "\"$INSTALL_DIR/runtime/bin/java\" -jar \"$INSTALL_DIR/app/jamal-cmd-${VERSION}.jar\" \"$@\"\n");
        write("jamal.bat","@echo off\n" +
                "set \"INSTALL_DIR=%~dp0..\"\n" +
                "\"%INSTALL_DIR%\\runtime\\bin\\java.exe\" -jar \"%INSTALL_DIR%\\app\\jamal-cmd-${VERSION}.jar\" %*\n");
    }
}
