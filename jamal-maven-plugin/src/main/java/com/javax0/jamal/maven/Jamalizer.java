package com.javax0.jamal.maven;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

@Mojo(name = "jamalize", requiresProject = false)
public class Jamalizer extends AbstractMojo {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger("jamal");
    public static final String ASCIIDOCTOR_DIR = ".asciidoctor";
    public static final String LIB_DIR = "lib";


    private static final String version = System.getProperty("jamal.version") == null ? Processor.jamalVersionString() : System.getProperty("jamal.version");

    void jamalize() throws IOException {
        LOG.info("Jamalizing the project using the version {} of Jamal", version);
        final Path newDir = createMvnDir(Path.of("."));
        writeExtensionsFile(newDir);
        try (final Stream<Path> walk = Files.walk(Path.of("."))) {
            for (final Path pp : walk
                    .filter(p -> p.toFile().isDirectory())
                    .map(Path::normalize)
                    .map(Path::toAbsolutePath)
                    .filter(p -> {
                        for (int i = 0; i < p.getNameCount(); i++) {
                            if (p.getName(i).toString().startsWith(".")) {
                                return false;
                            }
                        }
                        return true;
                    }) // skip hidden directories
                    .collect(Collectors.toList())) {
                createNewPomJam(pp);
            }
        }
        installAsciidoctorDirectory();
    }

    private void installAsciidoctorDirectory() {
        try {
            createAsccidoctorLibDir();
        } catch (IOException e) {
            LOG.error("Error creating the asciidoctor lib directory", e);
            return;
        }

        final var url = String.format("https://repo.maven.apache.org/maven2/com/javax0/jamal/jamal-asciidoc/%s/jamal-asciidoc-%s-jamal-asciidoc-distribution.zip", version, version);
        final var processor = new javax0.jamal.engine.Processor();
        final byte[] zipContent;
        try {
            LOG.info("Downloading the asciidoc distribution from {}", url);
            zipContent = FileTools.getFileBinaryContent(url, false, processor);
        } catch (BadSyntax e) {
            LOG.error("Error downloading the asciidoc distribution from {}", url, e);
            return;
        }
        try (final var is = new JarInputStream(new ByteArrayInputStream(zipContent))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    final var fn = entry.getName();
                    if (fn.endsWith(".jar")) {
                        final var path = Paths.get(ASCIIDOCTOR_DIR, LIB_DIR, fn);
                        try (final var fos = new FileOutputStream(path.toFile())) {
                            LOG.info("Extracting {}", path.normalize().toAbsolutePath().toFile().getAbsolutePath());
                            fos.write(is.readAllBytes());
                        }
                    }
                }

            }
        } catch (IOException e) {
            LOG.error("Error downloading the asciidoc distribution from {}", url, e);
        }
    }

    private void createAsccidoctorLibDir() throws IOException {
        final var asciidoctorDir = Paths.get(".", ASCIIDOCTOR_DIR);
        if (!Files.exists(asciidoctorDir)) {
            LOG.info("Creating the directory {}", asciidoctorDir.normalize().toAbsolutePath().toFile().getAbsolutePath());
            Files.createDirectory(asciidoctorDir);
        } else {
            LOG.info("The directory {} already exists", asciidoctorDir.normalize().toAbsolutePath().toFile().getAbsolutePath());
        }
        final var libDir = Paths.get(ASCIIDOCTOR_DIR, LIB_DIR);
        if (!Files.exists(libDir)) {
            LOG.info("Creating the directory {}", libDir.normalize().toAbsolutePath().toFile().getAbsolutePath());
            Files.createDirectory(libDir);
        } else {
            LOG.info("The directory {} already exists", libDir.normalize().toAbsolutePath().toFile().getAbsolutePath());
        }
    }

    /**
     * If {@code pom.jam} does not exist in the directory then create a new {@code pom.jam}
     * copying the existing {@code pom.xml} to {@code pom.jam}.
     *
     * @param p the project directory
     * @throws IOException when there is an error reading and writing some file
     */
    private void createNewPomJam(Path p) throws IOException {
        final var pom = Paths.get(p.toString(), "pom.xml");
        final var newPom = Paths.get(p.toString(), "pom.jam");
        if (Files.exists(pom) && !Files.exists(newPom)) {
            LOG.info("Creating pom.jam in {}", p.normalize().toAbsolutePath().toFile().getAbsolutePath());
            Files.copy(pom, newPom, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    /**
     * Create the {@code extensions.xml} file in the new {@code .mvn} directory.
     *
     * @param p the {@code .mvn} directory where the new file will be created
     * @throws IOException when there is an error reading and writing some file
     */
    private void writeExtensionsFile(Path p) throws IOException {
        LOG.info("Creating extensions.xml in {}", p.normalize().toAbsolutePath().toFile().getAbsolutePath());
        final var extensionsFile = Paths.get(p.toString(), "extensions.xml");
        Files.write(extensionsFile, ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<extensions>\n" +
                        "    <extension>\n" +
                        "        <groupId>com.javax0.jamal</groupId>\n" +
                        "        <artifactId>jamal-maven-extension</artifactId>\n" +
                        "        <version>" +
                        version
                        + "</version>\n" +
                        "    </extension>\n" +
                        "</extensions>\n").getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    /**
     * Create the {@code .mvn} directory if it is not there yet
     *
     * @param p the project directory
     * @return the new directory, or the one that already existed
     * @throws IOException when there is an error reading and writing some file
     */
    private Path createMvnDir(Path p) throws IOException {
        final var newDir = Paths.get(p.toString(), ".mvn");
        if (!Files.exists(newDir)) {
            LOG.info("Creating .mvn directory in {}", p.normalize().toAbsolutePath().toFile().getAbsolutePath());
            Files.createDirectory(newDir);
        } else {
            LOG.info("The directory {} already exists", newDir.normalize().toAbsolutePath().toFile().getAbsolutePath());
        }
        return newDir;
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            jamalize();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot jamalize", e);
        }
    }
}
