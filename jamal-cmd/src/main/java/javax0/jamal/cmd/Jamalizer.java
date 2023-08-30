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

    static void jamalize(String version) throws IOException, BadSyntax {
        if( version == null ){
            version = Jamalizer.version;
        }
        createAsccidoctorLibDir();
        extratZip(FileTools.getFileBinaryContent(
                String.format("https://repo.maven.apache.org/maven2/com/javax0/jamal/jamal-asciidoc/%s/jamal-asciidoc-%s-jamal-asciidoc-distribution.zip", version, version),
                false,
                new javax0.jamal.engine.Processor())
        );
    }

    private static void extratZip(final byte[] zipContent) throws IOException {
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
