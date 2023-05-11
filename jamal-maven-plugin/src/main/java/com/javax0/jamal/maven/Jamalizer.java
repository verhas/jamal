package com.javax0.jamal.maven;

import javax0.jamal.api.Processor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

@Mojo(name="jamalize")
public class Jamalizer extends AbstractMojo {
    void jamalize() throws IOException {
        final Path newDir = createMvnDir(Path.of("."));
        writeExtensionsFile(newDir);
        for (final Path pp : Files.walk(Path.of(".")).collect(Collectors.toList())) {
            createNewPomJam(pp);
        }
    }

    final static String[] fileNames = {"pom.xml", "pom.xml.jam", "pom.jam"};

    /**
     * If neither {@code pom.xml.jam}, nor {@code pom.jam} exists in the directory then create a new {@code pom.jam}
     * copying the existing {@code pom.xml} to {@code pom.jam}.
     * @param p the project directory
     * @throws IOException when there is an error reading and writing some file
     */
    private void createNewPomJam(Path p) throws IOException {
        final var pom = Paths.get(p.toString(), "pom.xml");
        final var newPom = Paths.get(p.toString(), "pom.jam");
        final var newPomPom = Paths.get(p.toString(), "pom.jam");
        if ( Files.exists(pom) && !Files.exists(newPom) && !Files.exists(newPomPom)) {
            Files.copy(pom, newPom, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    /**
     * Create the {@code extensions.xml} file in the new {@code .mvn} directory.
     * @param p the {@code .mvn} directory where the new file will be created
     *
     * @throws IOException when there is an error reading and writing some file
     */
    private void writeExtensionsFile(Path p) throws IOException {
        final var extensionsFile = Paths.get(p.toString(), "extensions.xml");
        Files.write(extensionsFile, ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<extensions>\n" +
                "    <extension>\n" +
                "        <groupId>com.javax0.jamal</groupId>\n" +
                "        <artifactId>jamal-maven-extension</artifactId>\n" +
                "        <version>" +
                Processor.jamalVersionString()
                + "</version>\n" +
                "    </extension>\n" +
                "</extensions>\n").getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    /**
     * Create the {@code .mvn} directory if it is not there yet
     * @param p the project directory
     * @return the new directory, or the one that already existed
     * @throws IOException when there is an error reading and writing some file
     */
    private Path createMvnDir(Path p) throws IOException {
        final var newDir = Paths.get(p.toString(), ".mvn");
        if (!Files.exists(newDir)) {
            Files.createDirectory(newDir);
        }
        return newDir;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            new Jamalizer().jamalize();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot jamalize",e);
        }
    }
}
