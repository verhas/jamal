package javax0.jamal.cmd;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.poi.word.XWPFProcessor;
import javax0.jamal.tools.CmdParser;
import javax0.jamal.tools.OutputFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is a command line file that can be used to process Jamal files starting Jamal from the command line.
 */
public class JamalMain {

    final static Set<String> parameters = Set.of(
            // snippet command_options
            "version",
            "verbose",
            "debug",
            "open",
            "close",
            "include",
            "exclude",
            "source",
            "target",
            "from",
            "to",
            "depth",
            "dry-dry-run",
            "dry-run",
            "docx",
            "help",
            "shcnf",
            "jamalize"
            // end snippet
    );

    public static void main(String[] args) {
        final var params = CmdParser.parse(args, parameters);
        if (params.get("jamalize").isPresent()) {
            try {
                Jamalizer.jamalize(params.get("version").orElse(null));
            } catch (Exception e) {
                throw new RuntimeException("Cannot jamalize the project", e);
            }
            return;
        }
        if (params.get("version").isPresent()) {
            System.out.printf("Jamal Version %s", javax0.jamal.api.Processor.jamalVersionString());
            return;
        }
        if (params.get("shcnf").isPresent()) {
            EnvironmentVariables.getProperties().forEach((k, v) -> System.out.printf("%s=%s\n", k, v));
            return;
        }
        if (params.get("help").isPresent()) {
            System.out.println("Usage: jamal [options] input output\n" +
                    // snippet command_options_help
                    "  -help                      help\n" +
                    "  -shcnf                     show the configuration values from ~/.jamal/settings.(properties|xml)\n" +
                    "  -version                   display version\n" +
                    "  -verbose                   print out the conversions\n" +
                    "  -open=<macroOpen>          the macro opening string\n" +
                    "  -close=<macroClose>        the macro closing string\n" +
                    "  -depth=<depth>             directory traversal depth, default is infinite\n" +
                    "  -debug=<debug>             type:port, usually http:8080\n" +
                    "  -include=<include>         file name regex pattern to include into the processing\n" +
                    "  -exclude=<exclude>         file name regex pattern to exclude from the processing\n" +
                    "  -source=<sourceDirectory>  source directory to start the processing\n" +
                    "  -target=<targetDirectory>  target directory to create the output\n" +
                    "  -from=<regex>              pattern for the file name transformation.\n" +
                    "  -to=<replacement>          replacement for the file name transformation.\n" +
                    "  -dry-dry-run               run dry, do not execute Jamal\n" +
                    "  -dry-run                   run dry, do not write result to output file\n" +
                    "  -docx                      treat the input as a docx, Microsoft Word file\n" +
                    "  -jamalize                  create the .asciidoctor/lib directory and download the Jamal Asciidoctor extension\n" +
                    // end snippet
                    "");
            return;
        }
        if (params.get("debug").isPresent()) {
            EnvironmentVariables.setenv(EnvironmentVariables.JAMAL_DEBUG_ENV, params.get("debug").get());
        }
        if (Arrays.stream("include,exclude,source,target,from,to,depth".split(",")).map(params::get).anyMatch(Optional::isPresent)) {
            final var sourceDirectory = params.get("source").orElse(".");
            final var depth = params.get("depth").map(Integer::parseInt).orElse(Integer.MAX_VALUE);
            final var include = params.get("include").orElse(".*jam$");
            final var exclude = params.get("exclude").orElse(null);
            final var includePredicate = getPathPredicate(include);
            final var excludePredicate = getPathPredicate(exclude).negate();
            try {
                Files.walk(Paths.get(sourceDirectory), depth)
                        .filter(Files::isRegularFile)
                        .filter(includePredicate)
                        .filter(excludePredicate)
                        .forEach(s -> executeJamal(s, params));
            } catch (IOException e) {
                throw new RuntimeException("Cannot process the files by Jamal. Something is wrong.", e);
            }
        } else {
            if (params.get(0).isEmpty() || params.get(1).isEmpty()) {
                throw new IllegalArgumentException("You must specify at least two arguments: input and output");
            }
            if (params.get(2).isPresent()) {
                throw new IllegalArgumentException("You cannot specify more than two arguments: input and output");
            }
            final var inputFile = params.get(0).get();
            final var outputFile = params.get(1).get();
            executeJamal(Paths.get(new File(inputFile).getAbsolutePath()), Paths.get(new File(outputFile).getAbsolutePath()), params);
        }
    }

    private static final System.Logger LOGGER = System.getLogger("JAMAL");

    private static void log(final System.Logger.Level level, final Position pos, final String format, final String... params) {
        final var msg = String.format(format, (Object[]) params) + (pos == null ? "" : " at ") + BadSyntaxAt.posFormat(pos);
        LOGGER.log(level, msg);
    }

    private static void executeJamal(final Path inputPath, CmdParser params) {
        executeJamal(inputPath.toAbsolutePath(), calculateTargetFile(inputPath, params).toAbsolutePath(), params);
    }

    private static void executeJamal(final Path inputPath, final Path outputPath, CmdParser params) {
        try {
            if (params.get("verbose").isPresent()) {
                System.out.println("Jamal " + inputPath.toString() + " -> " + outputPath);
            }
            final var drydry = params.get("dry-dry-run").isPresent();
            final var dry = params.get("dry-run").isPresent();
            final var macroOpen = params.get("open").orElse("{");
            final var macroClose = params.get("close").orElse("}");
            if (!drydry) {
                if (params.get("docx").isPresent()) {
                    final var processor = new XWPFProcessor(macroOpen, macroClose);
                    processor.setLogger(JamalMain::log);
                    processor.process(inputPath, dry ? null : outputPath);
                } else {
                    final String result;
                    try (final var processor = new Processor(macroOpen, macroClose)) {
                        processor.setLogger(JamalMain::log);
                        result = processor.process(createInput(inputPath));
                    }
                    if (!dry) {
                        writeOutput(outputPath, result);
                    }
                }
            }

        } catch (Exception e) {
            logException(e);
        }

    }

    /**
     * Write the result to the output file.
     * <p>
     * The method writes the content of the {@code result} string to the {@code output} file. The method creates the
     * parent directories if they do not exist. The method sets the file to read/write before writing and sets it back
     * to read only after writing. This is to avoid accidental modification of the file.
     *
     * @param output the file to write the result to
     * @param result the result to write
     * @throws IOException if the file cannot be written
     */
    private static void writeOutput(Path output, String result) throws IOException {
        try {
            OutputFile.save(output, result);
        } catch (Exception e) {
            logException(e);
        }
    }


    private static void logException(Exception e) {
        var sw = new StringWriter();
        var out = new PrintWriter(sw);
        e.printStackTrace(out);
        Arrays.stream(sw.toString().split("\n")).forEach(System.err::println);
    }

    private static Input createInput(Path inputFile) throws IOException {
        try (final var lines = Files.lines(inputFile)) {
            final var fileContent = lines.collect(Collectors.joining("\n"));
            return new javax0.jamal.tools.Input(fileContent, new Position(inputFile.toString(), 1));
        }
    }

    private static Path calculateTargetFile(final Path inputFile, CmdParser params) {
        final var sourceDirectory = params.get("source").orElse(".");
        final var targetDirectory = params.get("target").orElse(".");
        final var from = params.get("from").orElse("\\.jam$");
        final var to = params.get("to").orElse("");
        final var inputFileName = inputFile.toString();
        if (!inputFile.toString().replaceAll("\\\\", "/").startsWith(sourceDirectory)) {
            throw new IllegalArgumentException(
                    String.format("The input file '%s' is not in the source directory '%s'"
                            , inputFileName
                            , sourceDirectory));

        }
        return Paths.get((targetDirectory + inputFile.toString().substring(sourceDirectory.length()))
                .replaceAll(from, to));
    }


    /**
     * Convert the regular expression to a predicate. If the regular expression is null or empty string then the
     * predicate is constant false.
     *
     * @param param regular expression or simple wild card string to be converted to match predicate
     * @return the predicate.
     */
    private static Predicate<Path> getPathPredicate(final String param) {
        final Predicate<Path> predicate;
        if (param != null && param.length() > 0) {
            Pattern pattern = Pattern.compile(param);
            predicate = p -> pattern.matcher(p.toString()).find();
        } else {
            predicate = p -> false;
        }
        return predicate;
    }
}
