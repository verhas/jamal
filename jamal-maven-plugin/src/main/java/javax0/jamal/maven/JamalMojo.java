package javax0.jamal.maven;

import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * process all the files using jamal
 */
@Mojo(name = "jamal", requiresProject = false)
public class JamalMojo extends AbstractMojo {

    //<editor-fold desc="Configuration parameters" >
    @Parameter(defaultValue = "{")
    private String macroOpen;

    @Parameter(defaultValue = "}")
    private String macroClose;

    @Parameter(defaultValue = "${filePattern}")
    private String filePattern;

    @Parameter(defaultValue = "${jamalize}")
    private String jamalize;

    @Parameter(defaultValue = "${project.build.formatOutput}")
    private String formatOutput;

    @Parameter()
    private String exclude;

    @Parameter()
    private String sourceDirectory;

    @Parameter(defaultValue = "${project.build.sourceDirectory}", readonly = true)
    private String defaultSourceDirectory;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private String targetDirectory;

    @Parameter(defaultValue = "\\.jam$")
    private String transformFrom;

    @Parameter(defaultValue = "")
    private String transformTo;
    //</editor-fold>

    private boolean processingSuccessful;

    @Override
    public void execute() throws MojoExecutionException {
        normalizeConfiguration();
        final var log = getLog();
        log.debug("Jamal started");
        logParameters();
        normalizeDirectories();
        logParameters();
        if( "true".equals(jamalize)){
            try {
                new Jamalizer().jamalize();
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot jamalize",e);
            }
            return;
        }
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
                throw new MojoExecutionException("Cannot process the files by Jamal. Something is wrong.", e);
            }
            throw new MojoExecutionException("There was an error processing Jamal files. Have a look at the logs.", e);
        }
        if (!processingSuccessful) {
            throw new MojoExecutionException("There was an error processing Jamal files. Have a look at the logs.");
        }
    }

    private void executeJamal(final Path inputPath) {
        var log = getLog();
        log.debug("Jamal processing " + qq(inputPath.toString()));
        try {
            final var output = calculateTargetFile(inputPath);
            if (output != null) {
                log.debug("Jamal output for the file is " + qq(output.toString()));
                final String result;
                try (final var processor = new Processor(macroOpen, macroClose)) {
                    if ("true".equals(formatOutput)) {
                        result = formatOutput(output, processor.process(createInput(inputPath)));
                    } else {
                        result = processor.process(createInput(inputPath));
                    }
                }
                writeOutput(output, result);
            }
        } catch (Exception e) {
            logException(e, log::error);
            processingSuccessful = false;
        }
    }

    private String formatOutput(Path output, String result) {

        if (output.toString().endsWith(".xml")) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setValidating(false);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(new StringReader(result)));
                Transformer tf = TransformerFactory.newInstance().newTransformer();
                tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                Writer out = new StringWriter();
                tf.transform(new DOMSource(doc), new StreamResult(out));
                return Arrays.stream(out.toString().split(System.lineSeparator())).filter(s -> s.trim().length() > 0).collect(Collectors.joining(System.lineSeparator()));
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                getLog().debug(e);
                return result;
            }

        } else {
            return result;
        }
    }

    private void writeOutput(Path output, String result) throws IOException {
        try {
            Files.createDirectories(output.getParent());
        } catch (Exception e) {
            logException(e, getLog()::debug);
        }
        Files.write(output, result.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE);
    }

    private void logException(Exception e, Consumer<CharSequence> log) {
        var sw = new StringWriter();
        var out = new PrintWriter(sw);
        e.printStackTrace(out);
        Arrays.stream(sw.toString().split("\n")).forEach(log);
    }

    // snippet createInput
    private Input createInput(Path inputFile) throws IOException {
        try (final var lines = Files.lines(inputFile)) {
            final var fileContent = lines.collect(Collectors.joining("\n"));
            return new javax0.jamal.tools.Input(fileContent, new Position(inputFile.toString(), 1));
        }
    }
    // end snippet
    private Path calculateTargetFile(final Path inputFile) {
        final var log = getLog();
        final var inputFileName = inputFile.toString();
        if (!inputFile.toString().startsWith(sourceDirectory)) {
            log.error("The input file " + qq(inputFileName)
                + " is not in the source directory " + qq(sourceDirectory));
            processingSuccessful = false;
            return null;
        }
        return Paths.get((targetDirectory + inputFile.toString().substring(sourceDirectory.length()))
            .replaceAll(transformFrom, transformTo));
    }

    /**
     * Convert directory names ro normalized format
     */
    private void normalizeDirectories() throws MojoExecutionException {
        final var log = getLog();
        if (sourceDirectory == null) {
            sourceDirectory = Paths.get(defaultSourceDirectory + "/..").toString();
        }
        if (sourceDirectory == null) {
            log.error("sourceDirectory configuration parameter is null. Jamal needs source files to process.");
            throw new MojoExecutionException("sourceDirectory configuration parameter is null");
        }
        if (targetDirectory == null) {
            log.warn("targetDirectory is null. Jamal will not produce output but it will process the sources.");
        }

        sourceDirectory = Paths.get(sourceDirectory).normalize().toString();
        targetDirectory = Paths.get(targetDirectory).normalize().toString();
        if (!new File(sourceDirectory).exists()) {
            sourceDirectory = Paths.get(new File(".").getAbsolutePath()).normalize().toString();
        }
        if (!new File(targetDirectory).exists()) {
            targetDirectory = Paths.get(new File(".").getAbsolutePath()).normalize().toString();
        }
        log.debug("Source directory is " + sourceDirectory);
        log.debug("Target directory is " + targetDirectory);
    }

    private Predicate<Path> getPathPredicate(final String pattern) {
        final Predicate<Path> includePredicate;
        if (pattern != null && pattern.length() > 0) {
            Pattern includePattern = Pattern.compile(pattern);
            includePredicate = p -> includePattern.matcher(p.toString()).matches();
        } else {
            includePredicate = p -> false;
        }
        return includePredicate;
    }

    private String qq(String s) {
        return "'" + s + "'";
    }

    private void logParameters() {
        var log = getLog();
        log.debug("Configuration:");
        log.debug("    macroOpen=" + macroOpen);
        log.debug("    macroClose=" + macroClose);
        log.debug("    filePattern=" + filePattern);
        log.debug("    exclude=" + exclude);
        log.debug("    formatOutput=" + formatOutput);
        log.debug("    sourceDirectory=" + sourceDirectory);
        log.debug("    targetDirectory=" + targetDirectory);
        log.debug("    transform " + qq(transformFrom) + " -> " + qq(transformTo));
        log.debug("    jamalize=" + jamalize);
        log.debug("----");
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
        if (defaultSourceDirectory == null) {
            defaultSourceDirectory = ".";
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
