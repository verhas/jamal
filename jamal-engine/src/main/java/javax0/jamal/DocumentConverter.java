package javax0.jamal;

import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Input;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * Convert one single document.
 * <p>
 * Use this class and method to maintain your documentation in Jamal format. The project documentation can use macros
 * that fetch data automatically from the Java code. For example the value of a {@code static final} can be retrieved
 * using reflection. The snippet macro {@code java:field} does that. It can work only if the application is loaded and
 * on the classpath or module path.
 * <p>
 * The suggested practice is that you invoke {@link DocumentConverter#convert(String)} from your test code. That way the
 * application as well as the tests are on the classpath and are available to be referenced. If you  use the {@code
 * java:*} macros then you will automatically get the actual value of all fields documented and you get an error when
 * you alter the name of a class, method or field and you forget to follow the change in the documentation.
 */
public class DocumentConverter {
    /**
     * Create a JUnit test in your application that looks the following:
     *
     * <pre>
     *     @Test
     *     void generateDoc() throws Exception {
     *         DocumentConverter.convert("./README.adoc.jam");
     *     }
     * </pre>
     * <p>
     * This will convert the {@code README.adoc.jam} file to {@code README.adoc}. The name and the primary extension of
     * the file can be different.
     *
     * @param file the name of the Jamal source documentation file.
     * @throws Exception if the file does not exist, cannot be read, cannot be processed by Jamal (syntax error)
     */
    public static void convert(final String file) throws Exception {
        final var processor = new Processor("{%", "%}");
        final var in = FileTools.getInput(file, processor);
        final var result = processor.process(in);
        final var output = file.substring(0, file.length() - ".jam".length());
        FileTools.writeFileContent(output, result, processor);
    }

    private static class Includes {
        private final String[] includes;

        private Includes(String[] includes) {
            this.includes = includes;
        }

        Stream<String> stream() {
            return Arrays.stream(includes);
        }
    }

    private static class Excludes {
        private final String[] excludes;

        private Excludes(String[] excludes) {
            this.excludes = excludes;
        }

        Stream<String> stream() {
            return Arrays.stream(excludes);
        }
    }

    public static Includes include(String... s) {
        return new Includes(s);
    }

    public static Excludes exclude(String... s) {
        return new Excludes(s);
    }

    /**
     * Convert all files starting in the project root directory which match any of the 'include' patterns and do not match
     * any of the 'exclude' patterns.
     *
     * @param include the patterns to include.
     * @param exclude the patterns to exclude.
     * @throws Exception when there is an error in the conversion.
     */
    public static void convertAll(Includes include, Excludes exclude) throws Exception {
        for (final var p :
                Files.walk(Paths.get(getRoot()), Integer.MAX_VALUE)
                        .filter(Files::isRegularFile)
                        .filter(s -> include.stream().anyMatch(z -> s.toString().endsWith(z)) && exclude.stream().noneMatch(z -> s.toString().endsWith(z)))
                        .collect(Collectors.toList())) {
            executeJamal(p, Paths.get(p.toString().replaceAll("\\.jam$", "")));
        }
    }

    private static void executeJamal(final Path inputPath, final Path outputPath) throws Exception {
        final String result;
        try (final var processor = new Processor("{%", "%}")) {
            result = processor.process(createInput(inputPath));
        }
        Files.writeString(outputPath, result, StandardCharsets.UTF_8, WRITE, TRUNCATE_EXISTING, CREATE);
    }

    private static Input createInput(Path inputFile) throws IOException {
        try (final var lines = Files.lines(inputFile)) {
            var fileContent = lines.collect(Collectors.joining("\n"));
            return new javax0.jamal.tools.Input(fileContent, new Position(inputFile.toString(), 1));
        }
    }

    private static Optional<File> found(final StringBuilder sb) {
        File rootDirFile = new File(sb.toString());
        if (rootDirFile.exists()) {
            return Optional.of(rootDirFile);
        }
        sb.insert(0, "../");
        return Optional.empty();
    }

    /**
     * Get the root directory of the project.
     * <p>
     * The code is starting from the current working directory and going upward tries to find a directory that contains
     * a {@code .git}, {@code .mvn} or {@code ROOT.dir} file.
     * The directories {@code .git} and {@code .mvn} appear only in the root directory of a project and not in module
     * directories.
     * If the project is not in a git repository or in a project that does not have an {@code .mvn} directory then a
     * {@code ROOT.dir} placeholder has to be created in the root directory.
     *
     * @return the absolute path to the root directory of the project
     * @throws IOException when there is some error traversing the directories
     */
    public static String getRoot() throws IOException {
        final var dirs = Stream.of("ROOT.dir", ".git", ".mvn")
                .map(StringBuilder::new)
                .collect(Collectors.toList());

        final Optional<File> file =
                IntStream.range(0, 100).mapToObj(i ->
                                dirs.stream()
                                        .map(DocumentConverter::found)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .findFirst())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();

        if (file.isPresent()) {
            final var parent = file.get().getParentFile();
            return parent == null ? "." : parent.getCanonicalPath();
        } else {
            throw new RuntimeException("Cannot find the root directory of the project file.");
        }
    }

}
