package javax0.jamal.cmd;

import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is a command line file that can be used to process Jamal files starting Jamal from the command line.
 */
public class JamalMain implements Callable<Integer> {

    //<editor-fold desc="Configuration parameters" >

    @Option(names = {"--debug", "-g"}, defaultValue = "", description = "type:port, usually http:8080")
    private String debug = "";
    @Option(names = {"--open", "-o"}, defaultValue = "{", description = "the macro opening string")
    private String macroOpen = "{";
    @Option(names = {"--close", "-c"}, defaultValue = "}", description = "the macro closing string")
    private String macroClose = "}";
    @Option(names = {"--include", "-i"}, defaultValue = "*.jam$", description = "file name regex pattern to include into the processing")
    private String include = "*.jam$";
    @Option(names = {"--exclude", "-e"}, defaultValue = "", description = "file name regex pattern to exclude from the processing")
    private String exclude = null;
    @Option(names = {"--source", "-s"}, defaultValue = ".", description = "source directory to start the processing")
    private String sourceDirectory = ".";
    @Option(names = {"--target", "-t"}, defaultValue = ".", description = "target directory to create the output")
    private String targetDirectory = ".";
    @Option(names = {"--transform", "-r"}, defaultValue = Option.NULL_VALUE, arity = "1..2", description = "transformation from the input file name to the output file name")
    private String[] transform;
    @Option(names = {"--depth", "-d"}, description = "directory traversal depth, default is infinite")
    private int depth = Integer.MAX_VALUE;
    @Option(names = "--dry-dry-run", description = "run dry, do not execute Jamal")
    private boolean drydry = false;
    @Option(names = "--dry-run", description = "run dry, do not write result to output file")
    private boolean dry = false;
    @Option(names = {"--help", "-h"}, usageHelp = true, description = "help")
    private boolean help = false;
    @Option(names = {"--verbose", "-v"}, description = "verbose output")
    private boolean verbose = false;
    @Option(names = {"--regex", "-x"}, description = "interpret transform, include and exclude options as regex")
    private boolean regex = false;
    @Option(names = {"--file", "-f"}, description = "convert a single file, specify input and output")
    private boolean single = false;
    @Parameters(index = "0", defaultValue = Parameters.NULL_VALUE, arity = "0..1")
    private String inputFile;
    @Parameters(index = "1", defaultValue = Parameters.NULL_VALUE, arity = "0..1")
    private String outputFile;
    //</editor-fold>
    @Spec
    CommandSpec spec;
    private boolean processingSuccessful;

    public static void main(String[] args) {
        new CommandLine(new JamalMain()).execute(args);
    }


    public Integer call() {
        normalizeCommandInput();
        if (single) {
            if (inputFile == null || outputFile == null) {
                throw new IllegalArgumentException("When using the option -f/--file then you have to specify input and output file");
            }
            executeJamal(Paths.get(new File(inputFile).getAbsolutePath()), Paths.get(new File(outputFile).getAbsolutePath()));
        } else {
            final var includePredicate = getPathPredicate(include);
            final var excludePredicate = getPathPredicate(exclude).negate();
            processingSuccessful = true;
            try {
                Files.walk(Paths.get(sourceDirectory), depth)
                    .filter(Files::isRegularFile)
                    .filter(includePredicate)
                    .filter(excludePredicate)
                    .forEach(this::executeJamal);
            } catch (IOException e) {
                if (processingSuccessful) {
                    throw new RuntimeException("Cannot process the files by Jamal. Something is wrong.", e);
                }
                throw new RuntimeException("There was an error processing Jamal files. Have a look at the logs.", e);
            }
            if (!processingSuccessful) {
                throw new RuntimeException("There was an error processing Jamal files. Have a look at the logs.");
            }
        }
        return 0;
    }

    private void executeJamal(final Path inputPath) {
        executeJamal(inputPath, calculateTargetFile(inputPath));
    }

    private void executeJamal(final Path inputPath, final Path outputPath) {
        try {
            outlog("Jamal " + inputPath.toString() + " -> " + outputPath);
            if (outputPath != null) {
                if (!drydry) {
                    final String result;
                    EnvironmentVariables.setenv(EnvironmentVariables.JAMAL_DEBUG_ENV, debug);
                    try (final var processor = new Processor(macroOpen, macroClose)) {
                        result = processor.process(createInput(inputPath));
                    }
                    if (!dry) {
                        writeOutput(outputPath, result);
                    }
                }
            }
        } catch (Exception e) {
            logException(e);
            processingSuccessful = false;
        }
    }

    private void writeOutput(Path output, String result) throws IOException {
        try {
            final var parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            logException(e);
        }
        Files.write(output, result.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE);
    }

    private static void log(final String message, final PrintStream ps) {
        ps.println(message);
    }

    private static void errlog(final String message) {
        log(message, System.err);
    }

    private void outlog(final String message) {
        if (verbose) {
            log(message, System.out);
        }
    }

    private static void logException(Exception e) {
        var sw = new StringWriter();
        var out = new PrintWriter(sw);
        e.printStackTrace(out);
        Arrays.stream(sw.toString().split("\n")).forEach(JamalMain::errlog);
    }

    private Input createInput(Path inputFile) throws IOException {
        try (final var lines = Files.lines(inputFile)) {
            final var fileContent = lines.collect(Collectors.joining("\n"));
            return new javax0.jamal.tools.Input(fileContent, new Position(inputFile.toString(), 1));
        }
    }

    private Path calculateTargetFile(final Path inputFile) {
        final var inputFileName = inputFile.toString();
        if (!inputFile.toString().startsWith(sourceDirectory)) {
            errlog("The input file " + qq(inputFileName)
                + " is not in the source directory " + qq(sourceDirectory));
            processingSuccessful = false;
            return null;
        }
        return Paths.get((targetDirectory + inputFile.toString().substring(sourceDirectory.length()))
            .replaceAll(transform[0], transform[1]));
    }

    /**
     * Convert directory names to normalized format
     */
    private void normalizeCommandInput() throws RuntimeException {
        sourceDirectory = Paths.get(sourceDirectory).normalize().toString();
        if (sourceDirectory.length() == 0) {
            sourceDirectory = ".";
        }
        targetDirectory = Paths.get(targetDirectory).normalize().toString();
        if (targetDirectory.length() == 0) {
            targetDirectory = ".";
        }
        if (!new File(sourceDirectory).exists()) {
            throw new IllegalArgumentException(sourceDirectory + " does not exists.");
        }
        if (!new File(targetDirectory).exists()) {
            throw new IllegalArgumentException(targetDirectory + " does not exists.");
        }
        if (transform == null) {
            transform = new String[]{"\\.jam$", ""};
        }
        if (transform.length < 2) {
            transform = new String[]{transform[0], ""};
        }
    }

    /**
     * Convert the regular expression to a predicate. If the regular expression is null or empty string then the
     * predicate is constant false.
     *
     * @param param regular expression or simple wild card string to be converted to match predicate
     * @return the predicate.
     */
    private Predicate<Path> getPathPredicate(final String param) {
        final String regexString;
        if (regex) {
            regexString = param;
        } else {
            regexString = param.replaceAll("\\.", "\\.")
                .replaceAll("\\*", ".*")
                .replaceAll("\\?", ".");
        }
        final Predicate<Path> predicate;
        if (regexString != null && regexString.length() > 0) {
            Pattern pattern = Pattern.compile(regexString);
            predicate = p -> pattern.matcher(p.toString()).find();
        } else {
            predicate = p -> false;
        }
        return predicate;
    }

    private String qq(String s) {
        return "'" + s + "'";
    }

}
