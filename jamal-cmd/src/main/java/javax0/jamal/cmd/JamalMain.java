package javax0.jamal.cmd;

import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;

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
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is a command line file that can be used to process Jamal files starting Jamal from the command line.
 */
public class JamalMain {

    //<editor-fold desc="command line parameter strings">
    public static final String TRANSFORM_TO = "to";
    public static final String TRANSFORM_FROM = "from";
    public static final String MACRO_OPEN = "open";
    public static final String MACRO_CLOSE = "close";
    public static final String FILE_PATTERN = "pattern";
    public static final String EXCLUDE = "exclude";
    public static final String SOURCE_DIRECTORY = "source";
    public static final String TARGET_DIRECTORY = "target";
    //</editor-fold>

    //<editor-fold desc="Configuration parameters" >
    private String macroOpen = "{";
    private String macroClose = "}";
    private String filePattern = ".*\\.jam$";
    private String exclude = null;
    private String sourceDirectory = ".";
    private String targetDirectory = ".";
    private String transformFrom = "\\.jam$";
    private String transformTo = "";
    //</editor-fold>

    /**
     * Appends spaces to make the string 20 characters long.
     *
     * @param x the string to be padded
     * @return the string x padded with spaces
     */
    private static String arg(String x) {
        StringBuilder xBuilder = new StringBuilder(x);
        while (xBuilder.length() < 20) xBuilder.append(" ");
        return xBuilder.toString();
    }

    /**
     * Read the command line arguments and fill in the configuration parameters. Each command line argument should have
     * the {@code key=value} format.
     *
     * @param args the command line arguments
     */
    private void getCommandLineParams(String[] args) {
        for (final String arg : args) {
            final int eq = arg.indexOf('=');
            if (eq == -1) {
                displayError(arg);
            }
            final String key = arg.substring(0, eq);
            final String val = arg.substring(eq + 1);
            switch (key) {
                case MACRO_OPEN:
                    macroOpen = val;
                    break;
                case MACRO_CLOSE:
                    macroClose = val;
                    break;
                case FILE_PATTERN:
                    filePattern = val;
                    break;
                case EXCLUDE:
                    exclude = val;
                    break;
                case SOURCE_DIRECTORY:
                    sourceDirectory = val;
                    break;
                case TARGET_DIRECTORY:
                    targetDirectory = val;
                    break;
                case TRANSFORM_FROM:
                    transformFrom = val;
                    break;
                case TRANSFORM_TO:
                    transformTo = val;
                    break;
                default:
                    displayError(arg);
            }
        }
    }

    private void displayError(String arg) {
        if (!"help".equals(arg))
            System.err.println("The argument '" + arg + "' is malformed");
        System.err.println("Usage: jamal option1=value1 ... optionN=valueN\n"
            + arg(MACRO_OPEN) + "is the macro opening string, defaults to '{'\n"
            + arg(MACRO_CLOSE) + "is the macro closing string, defaults to '}'\n"
            + arg(FILE_PATTERN) + "include files that match this pattern (regex)\n"
            + arg(EXCLUDE) + "exclude files that match this pattern (regex)\n"
            + arg(SOURCE_DIRECTORY) + "the source directory, defaults to '.'\n"
            + arg(TARGET_DIRECTORY) + "where the generated files are to put, defaults to '.'\n"
            + arg(TRANSFORM_FROM) + "file name transformation pattern (regex), defaults to '\\.jam$'\n"
            + arg(TRANSFORM_TO) + "file name transformation pattern (string), defaults to ''\n"
            + arg(" ") + "    target_name = source_name.replaceAll(transformFrom,transformTo)\n"
            + arg(" ") + "    defaults to chopping off '.jam' extension\n"
        );
        System.exit(1);
    }

    public static void main(String[] args) {
        final var me = new JamalMain();
        me.getCommandLineParams(args);
        me.execute();
    }

    private boolean processingSuccessful;

    public void execute() {
        normalizeConfiguration();
        normalizeDirectories();
        final var includePredicate = getPathPredicate(filePattern);
        final var excludePredicate = getPathPredicate(exclude).negate();
        processingSuccessful = true;
        try {
            Files.walk(Paths.get(sourceDirectory))
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

    private void executeJamal(final Path inputPath) {
        try {
            final var outputPath = calculateTargetFile(inputPath);
            System.out.println("Jamal " + inputPath.toString() + " -> " + outputPath);
            if (outputPath != null) {
                final String result;
                try( final var processor = new Processor(macroOpen,macroClose)) {
                    result = processor.process(createInput(inputPath));
                }
                writeOutput(outputPath, result);
            }
        } catch (Exception e) {
            logException(e);
            processingSuccessful = false;
        }
    }

    private void writeOutput(Path output, String result) throws IOException {
        try {
            Files.createDirectories(output.getParent());
        } catch (Exception e) {
            logException(e);
        }
        Files.write(output, result.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE);
    }

    private void logException(Exception e) {
        var sw = new StringWriter();
        var out = new PrintWriter(sw);
        e.printStackTrace(out);
        Arrays.stream(sw.toString().split("\n")).forEach(System.err::println);
    }

    private Input createInput(Path inputFile) throws IOException {
        var fileContent = Files.lines(inputFile).collect(Collectors.joining("\n"));
        return new javax0.jamal.tools.Input(fileContent, new Position(inputFile.toString(), 1));
    }

    private Path calculateTargetFile(final Path inputFile) {
        final var inputFileName = inputFile.toString();
        if (!inputFile.toString().startsWith(sourceDirectory)) {
            System.err.println("The input file " + qq(inputFileName)
                + " is not in the source directory " + qq(sourceDirectory));
            processingSuccessful = false;
            return null;
        }
        return Paths.get((targetDirectory + inputFile.toString().substring(sourceDirectory.length()))
            .replaceAll(transformFrom, transformTo));
    }

    /**
     * Convert directory names to normalized format
     */
    private void normalizeDirectories() throws RuntimeException {
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
    }

    /**
     * Convert the regular expression to a predicate. If the regular expression is null or empty string then the
     * predicate is constant false.
     *
     * @param regex to be converted
     * @return the predicate.
     */
    private Predicate<Path> getPathPredicate(final String regex) {
        final Predicate<Path> predicate;
        if (regex != null && regex.length() > 0) {
            Pattern pattern = Pattern.compile(regex);
            predicate = p -> pattern.matcher(p.toString()).find();
        } else {
            predicate = p -> false;
        }
        return predicate;
    }

    private String qq(String s) {
        return "'" + s + "'";
    }

    private void logParameters() {
    }

    private void normalizeConfiguration() {
        if (macroOpen == null) {
            macroOpen = "";
        }
        if (macroClose == null) {
            macroClose = "";
        }
        if (filePattern == null) {
            filePattern = ".*\\.jam$";
        }
        if (exclude == null) {
            exclude = "";
        }
        if (sourceDirectory == null) {
            sourceDirectory = ".";
        }
        if (targetDirectory == null) {
            targetDirectory = ".";
        }
        if (transformFrom == null) {
            transformFrom = "\\.jam$";
        }
        if (transformTo == null) {
            transformTo = "";
        }

    }

}
