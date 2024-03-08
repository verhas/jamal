package javax0.jamal;

import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.OutputFile;

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

import static java.nio.file.StandardOpenOption.*;

/**
 * Convert one single document.
 * <p>
 * Use this class and method to maintain your documentation in Jamal format. The project documentation can use macros
 * that fetch data automatically from the Java code. For example the value of a {@code static final} can be retrieved
 * using reflection. The s.nippet macro {@code java:field} does that. It can work only if the application is loaded and
 * on the classpath or module path.
 * <p>
 * The suggested practice is that you invoke {@link DocumentConverter#convert(String)} from your test code. That way the
 * application as well as the tests are on the classpath and are available to be referenced. If you  use the {@code
 * java:*} macros then you will automatically get the actual value of all fields documented and you get an error when
 * you alter the name of a class, method or field and you forget to follow the change in the documentation.
 */
public class DocumentConverter {
    public static final String[] PLACEHOLDERS = {
            // snippet PLACEHOLDERS
            "ROOT.dir",
            ".git",
            ".mvn",
            "package.json",
            "requirements.txt",
            "Pipfile",
            "Gemfile",
            "Cargo.toml",
            "CMakeLists.txt",
            ".sln",
            "go.mod",
            ".travis.yml",
            ".gitlab-ci.yml",
            "azure-pipelines.yml",
            // end snippet
    };

    /**
     * Convert a document preprocessing and save the result into a file.
     * The source file is supposed to have the {@code .jam} extension.
     * The output file will have the same name as the input, but without the extension.
     * This method can be used to execute Jamal programmatically.
     * <p>
     * For example, you can create a JUnit test in your application that looks the following:
     *
     * <pre>
     *     \@Test
     *     void generateDoc() throws Exception {
     *         DocumentConverter.convert("./README.adoc.jam");
     *     }
     * </pre>
     * <p>
     * This will convert the {@code README.adoc.jam} file to {@code README.adoc}.
     * <p>
     * The use of this method from Unit tests can be avoided after version 2.0.0 using the maven plugin and the
     * dynamic macro package loading.
     *
     * @param file the name of the Jamal source documentation file.
     * @throws Exception if the file does not exist, cannot be read, cannot be processed by Jamal (syntax error)
     */
    public static void convert(final String file) throws Exception {
        convert(file, false);
    }

    /**
     * This method is the same as {@link #convert(String)} but it can also specify if the default separators should be
     * used. When the {@code useDefaultSeparators} is {@code true} then the default separators {@code { } }.
     * are used. When the {@code useDefaultSeparators} is {@code false} then the separators are
     * {@code {% %}}.
     * <p>
     * This method is used by the asciidoc plugin.
     *
     * @param file                 the name of the Jamal source documentation file.
     * @param useDefaultSeparators signals if the processing should use the default separators.
     * @throws Exception if the file does not exist, cannot be read, cannot be processed by Jamal (syntax error)
     */
    public static void convert(final String file, boolean useDefaultSeparators) throws Exception {
        final var output = file.substring(0, file.length() - ".jam".length());
        convert(file, output, useDefaultSeparators);
    }

    /**
     * Convert a document preprocessing and save the result into a file.
     *
     * @param file   the name of the Jamal source documentation file.
     * @param output the name of the output file.
     * @throws Exception if the file does not exist, cannot be read, cannot be processed by Jamal (syntax error)
     */
    public static void convert(final String file, final String output) throws Exception {
        convert(file, output, false);
    }

    /**
     * This method is the same as {@link #convert(String)} but it can also specify if the default separators should be
     * used. When the {@code useDefaultSeparators} is {@code true} then the default separators {@code { } }.
     * are used. When the {@code useDefaultSeparators} is {@code false} then the separators are
     * {@code {% %}}.
     *
     * @param file                 the name of the Jamal source documentation file.
     * @param output               the name of the output file.
     * @param useDefaultSeparators signals if the processing should use the default separators.
     * @throws Exception if the file does not exist, cannot be read, cannot be processed by Jamal (syntax error)
     */
    public static void convert(final String file, final String output, boolean useDefaultSeparators) throws Exception {
        final var processor = useDefaultSeparators ? new Processor() : new Processor("{%", "%}");
        final var in = FileTools.getInput(file, processor);
        final var result = processor.process(in);
        new OutputFile(processor).save(Paths.get(output), result);
    }

    public static class Includes {
        private final String[] includes;

        private Includes(String[] includes) {
            this.includes = includes;
        }

        Stream<String> stream() {
            return Arrays.stream(includes);
        }
    }

    public static class Excludes {
        private final String[] excludes;

        private Excludes(String[] excludes) {
            this.excludes = excludes;
        }

        Stream<String> stream() {
            return Arrays.stream(excludes);
        }
    }

    /**
     * Create an {@link Includes} object that can be used to specify the files that should be converted.
     * <p>
     * The resulting object can be used in the {@link #convertAll(Includes, Excludes)} method.
     *
     * @param s the patterns to include.
     * @return the {@link Includes} object.
     */
    public static Includes include(String... s) {
        return new Includes(s);
    }

    /**
     * Create an {@link Excludes} object that can be used to specify the files that should not be converted.
     * <p>
     * The resulting object can be used in the {@link #convertAll(Includes, Excludes)} method.
     *
     * @param s the patterns to exclude.
     * @return the {@link Excludes} object.
     */
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
        try (final var paths = Files.walk(Paths.get(getRoot()), Integer.MAX_VALUE)) {
            for (final var p : paths.filter(Files::isRegularFile)
                    .filter(s -> include.stream().anyMatch(z -> s.toString().endsWith(z)) && exclude.stream().noneMatch(z -> s.toString().endsWith(z)))
                    .collect(Collectors.toList())) {
                executeJamal(p, Paths.get(p.toString().replaceAll("\\.jam$", "")));
            }
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


    /**
     * Check that the file pointed by the {@code sb} StringBuilder exists. If it does not exist, then go to one
     * directory up, prepending {@code ../} to the {@code sb} and return an optional empty. The caller can create a
     * loop calling the method many times.
     * <p>
     * If the file exists, then return an optional that contains the file.
     *
     * @param sb the StringBuilder that contains the path to the file
     * @return an optional that contains the file if it exists or an empty optional if it does not exist
     */
    private static Optional<File> found(final StringBuilder sb) {
        final File rootDirFile = new File(sb.toString());
        if (rootDirFile.exists()) {
            return Optional.of(rootDirFile);
        }
        sb.insert(0, "../");
        return Optional.empty();
    }

    /**
     * Get the root directory of the project
     * <p>
     * This method calls {@link #getRoot(String...)} with an extensive predefined set of file names that are searched
     * for. The set of files is stable for most programming projects and build systems.
     *
     * @return the absolute path to the root directory of the project
     * @throws IOException when there is some error traversing the directories
     */
    public static String getRoot() throws IOException {
        return getRoot(PLACEHOLDERS
        );
    }

    /**
     * Get the root directory of the project.
     * <p>
     * The code is starting from the current working directory, and going upward tries to find a directory that contains
     * one of the files or directories given in the argument.
     *
     * @param rootFiles the names of the files that are searched for in the root directory
     * @return the absolute path to the root directory of the project
     * @throws IOException      when there is some error traversing the directories
     * @throws RuntimeException when the root directory cannot be found
     */
    public static String getRoot(String... rootFiles) throws IOException {
        final var dirs = Arrays.stream(rootFiles)
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
            final var projectRoot = file.get().getParentFile();
            return projectRoot == null ? "." : projectRoot.getCanonicalPath();
        } else {
            throw new RuntimeException("Cannot find the root directory of the project file.");
        }
    }

}
