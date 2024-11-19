package javax0.jamal.asciidoc;

import javax0.jamal.DocumentConverter;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;
import javax0.jamal.api.TransientException;
import javax0.jamal.asciidoc258.Asciidoctor2XXCompatibilityProxy;
import javax0.jamal.asciidoc258.CompatibilityProcess;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.*;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.extension.Reader;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.nonNull;

public class JamalPreprocessor extends Preprocessor implements ExtensionRegistry, CompatibilityProcess {
    /**
     * The result structure of the execution of in-process Jamal.
     */
    private static class Result {
        String result = null;
        Position position = new Position("", 0, 0);
        String errorMessage = null;
        Exception exception = null;

        String log;

        List<String> lines;
        Processor processor;
    }

    @Override
    public void register(Asciidoctor asciidoctor) {
        final var registry = asciidoctor.javaExtensionRegistry();
        registry.preprocessor((Preprocessor) Asciidoctor2XXCompatibilityProxy.create(this));
    }

    private String getAsciidoctorJVersion() {
        try (final var in = getClass().getResourceAsStream("/META-INF/asciidoctorj-version.properties")) {
            if (in == null) {
                return "N/A";
            }
            Properties versionProps = new Properties();
            versionProps.load(in);
            return versionProps.getProperty("version.asciidoctorj");
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * This integer holds a counter counting how many times the preprocessor was invoked.
     * This is used for logging purposes only.
     */
    private static final AtomicInteger runCounter = new AtomicInteger(0);

    /**
     * This is the reference that holds the last run.
     * <p>
     * The ASCIIDOCTOR plugin invokes the rendering many times (two or three sometimes) even, when the source has not
     * changed. Rendering an AsciiDoc document is not cheap in itself, but Jamal processing AND rendering can be
     * extremely expensive. You can do extensive things programming, Jamal.
     * <p>
     * To avoid unneeded processing, Jamal is only executed if the source code has been changed since the last
     * execution. If the MD5 signature of the input is the same as the last execution, then we just return whatever
     * the return value was during the last execution. There is no reason to store more than one item, therefore,
     * this cache is static.
     */
    private static final AtomicReference<ProcessingCache> cache = new AtomicReference<>(new ProcessingCache(null, null, null));

    final static ExecutorService fileConverterExecutor = Executors.newSingleThreadExecutor();

    final static Map<String, Integer> fileCounter = new HashMap<>();

    private static class FileConverter implements Runnable {

        private final String inputFile;
        private final int counter;
        private final InFileOptions opts;

        FileConverter(String inputFile, InFileOptions opts) {
            synchronized (fileCounter) {
                this.inputFile = inputFile;
                if (fileCounter.containsKey(inputFile)) {
                    counter = fileCounter.get(inputFile) + 1;
                } else {
                    counter = 0;
                }
                fileCounter.put(inputFile, counter);
                this.opts = opts;
            }
        }

        @Override
        public void run() {
            synchronized (FileConverter.class) {
                synchronized (fileCounter) {
                    final var last = fileCounter.get(inputFile);
                    if (last > counter) {
                        return;
                    }
                }
                try {
                    DocumentConverter.convert(inputFile, opts.useDefaultSeparators);
                } catch (Exception ignore) {
                    // there is not much we can do here to signal the error,
                    // and the synchronous processing will hopefully get the same error
                }
            }

        }
    }

    @Override
    public Reader process(Document document, PreprocessorReader reader) {
        final var runCounter = JamalPreprocessor.runCounter.incrementAndGet();
        final var fileName = nonNull(reader.getFile()) ? reader.getFile() : (String) document.getAttribute("docfile");
        setContextClassLoader();
        /*
         * The plugin is invoked for all AsciiDoc files.
         * If the file ending is adoc, AsciiDoc or anything else, then there is nothing to do with the Jamal preprocessor.
         */
        if (!fileName.endsWith(".jam")) {
            return reader;
        }
        final var linesAfterFM = reader.readLines();
        // snipline fetch-font-matter
        final var frontMatter = document.getAttribute("front-matter", null);
        final var lines = new ArrayList<String>();
        if (frontMatter instanceof String) {
            lines.add("---");
            lines.addAll(List.of(((String) frontMatter).split("\n", -1)));
            lines.add("---");
        }
        lines.addAll(linesAfterFM);

        var outputFileName = fileName.substring(0, fileName.length() - 4);
        final var firstLine = lines.isEmpty() ? "" : lines.get(0).trim();
        final var opts = new InFileOptions(firstLine);
        if (opts.off) {
            reader.restoreLines(lines);
            return reader;
        }

        if (opts.dual && !opts.fromFile) {
            fileConverterExecutor.submit(new FileConverter(fileName, opts));
        }

        if (opts.fromFile) {
            replaceTheLinesFromTheFile(fileName, lines);
        }

        final var log = new Log(outputFileName, opts.log, runCounter);

        log.info("started " + outputFileName);
        final var text = String.join("\n", lines);
        final String md5 = Md5Calculator.md5(text);
        log.info("md5 " + md5);

        final var myCache = cache.get();
        final var cachingFileReader = new CachingFileReader(opts.withoutDeps);
        final List<String> newLines;
        if (myCache.isTheSame(md5)) {
            newLines = myCache.lines;
            cachingFileReader.files.putAll(myCache.files);
            log.info("restored");
        } else {
            if (opts.external) {
                newLines = JamalExecutor.execute(fileName, lines);
                log.info("setting cache");
                JamalPreprocessor.cache.set(new ProcessingCache(md5, newLines, cachingFileReader));
                if (opts.save) {
                    writeOutputFile(null, outputFileName, log, cachingFileReader, newLines, opts);
                }
            } else {
                final var logger = opts.prefixLog ? new StringColletingLogger() : new javax0.jamal.api.Processor.Logger() {
                    @Override
                    public void log(final System.Logger.Level level, final Position pos, final String format, final String... params) {

                    }

                    @Override
                    public String toString() {
                        return "";
                    }
                };
                final var result = runJamalInProcess(fileName, lines, opts, text, cachingFileReader, logger);
                newLines = result.lines;
                outputFileName = getSaveToFileName(fileName, outputFileName, result);
                if (!(result.exception instanceof TransientException)) {
                    log.info("setting cache");
                    JamalPreprocessor.cache.set(new ProcessingCache(md5, newLines, cachingFileReader));
                }
                if (opts.save) {
                    writeOutputFile(result.processor, outputFileName, log, cachingFileReader, newLines, opts);
                }
            }
        }
        restoreTheLinesIntoThePlugin(reader, fileName, log, newLines, opts);
        log.info("DONE");
        return reader;
    }

    private static void replaceTheLinesFromTheFile(final String fileName, final ArrayList<String> lines) {
        try {
            final var fileLines = Files.readAllLines(Path.of(fileName), StandardCharsets.UTF_8);
            // only if the file was read, it skips in the case of exception
            lines.clear();
            lines.addAll(fileLines);
        } catch (IOException e) {
            // just ignore
        }
    }

    private static String getSaveToFileName(final String fileName, String outputFileName, final Result result) {
        try {
            final var output = MacroReader.macro(result.processor).readValue("asciidoc:output").orElse(null);
            if (output != null) {
                final var outputFile = new File(FileTools.absolute(fileName, output));
                if (outputFile.exists() && outputFile.isDirectory()) {
                    final var outputFileNameFile = new File(outputFileName);
                    return new File(outputFile, outputFileNameFile.getName()).getAbsolutePath();
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    outputFile.getParentFile().mkdirs();
                    return outputFile.getAbsolutePath();
                }
            }
        } catch (BadSyntax ignored) {
            // the macro may have parameters: we do not use it in that case
        }
        return outputFileName;
    }

    /**
     * Set the context class loader to the preprocessors class loader.
     * <p>
     * Snake yaml is part of the Yaml macro library, but it is also used by the Asciidoctor plugin.
     * When Snake Yaml code tries to access the class {@link javax0.jamal.api.Ref} it uses, it tries to load the class
     * calling the context class loader and then the plugin's class loader.
     * <p>
     * The plugin, eventually separates itself from the preprocessor.
     * It loads the preprocessor with a special class loader. (At least it seems like that.)
     * That class loader does not see any Jamal library.
     * <p>
     * Hence, to help the situation, we set here the context class loader.
     */
    private void setContextClassLoader() {
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    }

    private Result runJamalInProcess(final String fileName, final List<String> lines, final InFileOptions opts, final String text, final CachingFileReader cachingFileReader, final javax0.jamal.api.Processor.Logger logger) {
        final var processor = opts.useDefaultSeparators ? new Processor() : new Processor(Configuration.INSTANCE.macroOpen, Configuration.INSTANCE.macroClose);
        processor.setLogger(logger);
        processor.setFileReader(cachingFileReader);
        final var input = Input.makeInput(text, new Position(fileName, 0, 0));
        // snipline spec_env filter="(.*?)"
        System.setProperty("intellij.asciidoctor.plugin", "1");
        // snipline spec_env2 filter="(.*?)"
        System.setProperty("asciidocfx.asciidoctor.plugin", "1");
        try {
            // set to fail fast, because it is important to finish sooner than to discover all the errors like in a batch,
            // also additional errors may be a distraction. As soon as the first error is fixed, the next error will be
            // displayed.
            OptionsStore.getInstance(processor).addOptions(Processor.FAIL_FAST);
            // snipline asciidoctorj_version filter="(.*?)"
            processor.defineGlobal(processor.newUserDefinedMacro("asciidoctorj:version", getAsciidoctorJVersion()));
        } catch (BadSyntax ignore) {
        }
        final var r = processJamal(processor, input);
        r.processor = processor;
        if (opts.writableOutput) {
            // we do not allow writableOutput when processing is internal
            r.position = new Position("", 0);
            r.exception = new BadSyntax("writableOutput can only be used when processing is external");
            r.errorMessage = r.exception.getMessage();
        }
        r.lines = postProcess(lines, r, fileName);
        return r;
    }

    /**
     * Write the output file and set it to read only for the owner of the file.
     * It may prevent accidental editing of the generated file.
     *
     * @param outputFileName    the name of the output file
     * @param log               the logger
     * @param cachingFileReader the file reader that contains the list of the files that were read. It is used to log.
     * @param newLines          the lines that are to be written to the output file
     * @param opts              the options that were read from the first line of the input file
     */
    private void writeOutputFile(final Processor processor, final String outputFileName, final Log log, final CachingFileReader cachingFileReader, final List<String> newLines, InFileOptions opts) {
        try {
            Path output = Path.of(outputFileName);
            final String out = String.join("\n", newLines);
            if (processor == null) {
                new OutputFile(StandardCharsets.UTF_8, opts.writableOutput).save(output, out);
            } else {
                new OutputFile(processor).save(output, out);
            }
            log.info("saved");
            log.info("dependencies\n" + cachingFileReader.list());
        } catch (Exception e) {
            log.info("Failed to save the output file " + outputFileName);
            try (StringWriter sw = new StringWriter();
                 PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                log.info("Exception was:\n" + sw);
            } catch (IOException ignore) {
            }
        }
    }

    private static final List<Converter> converters = Converter.getInstances();

    /**
     * Convert the lines to Asciidoc and then restore them to the IntelliJ Asciidoctor plugin.
     * <p>
     * The code asks each converted loaded by the service loader if that can accommodate the conversions.
     * The simplest conversion os the one that converts from Asciidoc to Asciidoc doing nothing.
     * There is also a markdown converter supplied in the application.
     * Any other converter can be copied into the .asciidoctor/lib directory, it will work.
     * <p>
     * If there is no converter, then the input is treated as plaintext and converted to preformatted text in Asciidoc.
     *
     * @param reader   the reader of the IntelliJ plugin to be used to restore the lines
     * @param fileName the original name of the file, with the {@code .jam} extension
     * @param log      logger
     * @param lines    the lines of the input to be converted and saved to the Asciidoc editor IntelliJ plugin
     * @param opts     input file options, to decide if the front matter is to be kept in the file
     */
    private void restoreTheLinesIntoThePlugin(final PreprocessorReader reader, final String fileName, final Log log, final List<String> lines, final InFileOptions opts) {
        for (final var converter : converters) {
            if (converter.canConvert(fileName)) {
                final var convertedLines = converter.convert(lines);
                log.info("not adding prelude and post lude, it is an asciidoc file");
                if (opts.keepFrontMatter || convertedLines.isEmpty() || !convertedLines.get(0).equals("---")) {
                    log.info("Keeping the front matter, or no front matter");
                    reader.restoreLines(convertedLines);
                } else {
                    final var firstLine = lineIndexAfterTheFrontMatter(convertedLines);
                    for (int i = convertedLines.size() - 1; i >= firstLine; i--) {
                        reader.restoreLine(convertedLines.get(i));
                    }
                }
                return;
            }
        }
        log.info("adding pre and post ludes");
        reader.restoreLines(TextConverter.convert(fileName, lines));
    }

    /**
     * Search for the end of the front-matter.
     * Front-matter is the part at the start of the asciidoc file that starts with, and ends with a {@code ---} line.
     * It is used by Jekyll and some other site builder tools, and it is ignored by asciidoc.
     * The Jamal plugin puts this front-matter back at the start of the file before processing.
     * In case there is any front-matter after the Jamal processing, it is removed so that Asciidoc processing gets the
     * lines it was expecting.
     * <p>
     * It is assumed that the file starts with a line {@code ---}. This is not checked.
     *
     * @param lines the lines that contain the font-matter
     * @return the index of the first line after the front matter or zero if there is no end to the front-matter before
     * the last line
     */
    private static int lineIndexAfterTheFrontMatter(final List<String> lines) {
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).equals("---")) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Modify the output according to the errors of the Jamal processing.
     *
     * @param lines         the original lines of the input. It is used when there was an error.
     * @param r             the result that contains the possible errors as well as the processed output
     * @param inputFileName the name of the input file used for error display only-
     * @return the list of the lines to be used by the asciidoctor processor
     */
    private List<String> postProcess(final List<String> lines, final Result r, final String inputFileName) {
        if (r.errorMessage == null && r.log.isEmpty() && r.result != null) {
            return List.of(r.result.split("\n"));
        } else if (r.errorMessage == null && !r.log.isEmpty()) {
            List<String> newLines = new ArrayList<>();
            appendLog(r.log, newLines);
            newLines.addAll(List.of(r.result.split("\n")));
            return newLines;
        } else {
            List<String> newLines = new ArrayList<>();

            appendLog(r.log, newLines);
            appendError(r.errorMessage, newLines);
            final int errorLineNo = copyLinesPriorTheError(lines, r, newLines);
            appendError(r.errorMessage, newLines);
            copyLinesPastTheError(lines, newLines, errorLineNo);
            appendError(r.errorMessage, newLines);
            appendExceptionDump(r, inputFileName, newLines);

            return newLines;
        }
    }

    private static void appendExceptionDump(final Result r, final String inputFileName, final List<String> lines) {
        if (r.exception != null) {
            lines.add("[source]");
            lines.add("----");
            lines.addAll(List.of(ExceptionDumper.dump(r.exception, inputFileName).toString().split("\n")));
            lines.add("----");
        }
    }

    private static void copyLinesPastTheError(final List<String> lines, final List<String> newLines, final int errorLineNo) {
        for (int i = errorLineNo; i < lines.size(); i++) {
            newLines.add(lines.get(i));
        }
    }

    private static int copyLinesPriorTheError(final List<String> lines, final Result r, final List<String> newLines) {
        int i;
        for (i = 0; i < r.position.line && i < lines.size(); i++) {
            newLines.add(lines.get(i));
        }
        return i;
    }

    private JamalPreprocessor.Result processJamal(final Processor processor, final Input input) {
        final Result r = new Result();
        try {
            r.result = processor.process(input);
            r.log = processor.logger().toString();
        } catch (BadSyntaxAt bs) {
            r.position = bs.getPosition();
            r.errorMessage = bs.getMessage();
            r.exception = bs;
            r.log = processor.logger().toString();
        } catch (Exception bs) {
            r.position = new Position("", 0);
            r.errorMessage = bs.getMessage();
            r.exception = bs;
            r.log = processor.logger().toString();
        }
        return r;
    }

    private void appendError(final String errorMessage, final List<String> newLines) {
        if (errorMessage != null) {
            newLines.add("[WARNING]");
            newLines.add("--");
            Collections.addAll(newLines, Arrays.stream(errorMessage.split("\n")).map(s -> "* " + s).toArray(String[]::new));
            newLines.add("--");
        }
    }

    private void appendLog(final String log, final List<String> newLines) {
        if (log != null && !log.isEmpty()) {
            newLines.add("[NOTE]");
            newLines.add("--");
            Collections.addAll(newLines, Arrays.stream(log.split("\n")).map(s -> "* " + s).toArray(String[]::new));
            newLines.add("--");
        }
    }
}