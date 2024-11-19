package javax0.jamal.cmd;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

public class Jamalizer {
    public static final String ASCIIDOCTOR_DIR = ".asciidoctor";
    public static final String LIB_DIR = "lib";


    private static final String version = System.getProperty("jamal.version") == null ? Processor.jamalVersionString() : System.getProperty("jamal.version");
    private static final String DOWNLOAD_URL_TEMPLATE = "https://repo.maven.apache.org/maven2/com/javax0/jamal/jamal-asciidoc/%s/jamal-asciidoc-%s-jamal-asciidoc-distribution.zip";
    private static final String LIVE_TEMPLATES_ZIP = "live-templates.zip";
    private static final String DOWNLOAD_URL_LIVETEMPLATE = "https://github.com/verhas/jamal/raw/refs/tags/v%s/jamal-asciidoc/"+LIVE_TEMPLATES_ZIP;

    static void jamalize(String version) throws IOException, BadSyntax {
        if (version == null) {
            version = Jamalizer.version;
        }
        warnAboutSnapshot(version);
        createAsccidoctorLibDir();
        extractZip(FileTools.getFileBinaryContent(
                String.format(DOWNLOAD_URL_TEMPLATE, version, version),
                false,
                new javax0.jamal.engine.Processor())
        );
        final var liveTemplates = FileTools.getFileBinaryContent(
                String.format(DOWNLOAD_URL_LIVETEMPLATE, version),
                false,
                new javax0.jamal.engine.Processor());
        final var path = Paths.get(ASCIIDOCTOR_DIR, LIB_DIR, LIVE_TEMPLATES_ZIP);
        try (final var fos = new FileOutputStream(path.toFile())) {
            fos.write(liveTemplates);
        }
    }

    private static void warnAboutSnapshot(String version) {
        if (version.contains("SNAPSHOT")) {
            System.err.println("The version of the Jamalizer is a SNAPSHOT version.\n" +
                    "This is not recommended for production use.\n" +
                    "Since the command 'jamalize' downloads Jamal from Maven central using the URL\n\n" +
                    String.format(DOWNLOAD_URL_TEMPLATE, version, version) +
                    "\n\nit is possible that the version is not available in the repository yet.\n" +
                    "Use 'jamal -jamalize version=...' to specify a version that is available in the repository."
            );
        }
    }

    /**
     * Extracts JAR files from a ZIP archive provided as a byte array.
     *
     * <p>This method processes the given byte array, which represents the content
     * of a ZIP archive. It scans the archive, looking for entries that are JAR files
     * (files with a ".jar" extension). For each JAR file found, it writes the content
     * to a specified directory on the filesystem.</p>
     *
     * <p>The output JAR files are extracted to the path specified by the
     * concatenation of {@code ASCIIDOCTOR_DIR} and {@code LIB_DIR}, using
     * the JAR filename from the ZIP entry.</p>
     *
     * @param zipContent the byte array containing the ZIP archive content
     * @throws IOException if an I/O error occurs while reading the ZIP archive
     *                     or writing the extracted JAR files to the filesystem
     */
    private static void extractZip(final byte[] zipContent) throws IOException {
        try (final var is = new JarInputStream(new ByteArrayInputStream(zipContent))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    final var fn = entry.getName();
                    if (fn.endsWith(".jar")) {
                        final var path = Paths.get(ASCIIDOCTOR_DIR, LIB_DIR, fn);
                        try (final var fos = new FileOutputStream(path.toFile())) {
                            fos.write(is.readAllBytes());
                        }
                    }
                }
            }
        }
    }

    /**
     * Create the {@code .asciidoctor/lib} directory if it is not there yet.
     * <p>
     * If there was any file in the directory then delete them, but it does
     * not delete subdirectories, no need.
     *
     * @throws IOException if the directory cannot be created.
     */
    private static void createAsccidoctorLibDir() throws IOException {
        final var asciidoctorDir = Paths.get(".", ASCIIDOCTOR_DIR);
        if (!Files.exists(asciidoctorDir)) {
            Files.createDirectory(asciidoctorDir);
        }
        final var libDir = Paths.get(ASCIIDOCTOR_DIR, LIB_DIR);
        if (!Files.exists(libDir)) {
            Files.createDirectory(libDir);
        }
        try (var files = Files.walk(libDir)) {
            files.filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

}
