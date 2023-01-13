package javax0.jamal.asciidoc;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.MacroReader;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static nl.jworks.markdown_to_asciidoc.Converter.convertMarkdownToAsciiDoc;

public class JamalPreprocessor extends Preprocessor implements ExtensionRegistry {
    /**
     * The result structure of the execution of in-process Jamal.
     */
    private static class Result {
        String result = null;
        Position position = new Position("", 0, 0);
        String errorMessage = null;
        Exception exception = null;

        List<String> lines;
        Processor processor;
    }

    @Override
    public void register(Asciidoctor asciidoctor) {
        JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        javaExtensionRegistry.preprocessor(JamalPreprocessor.class);
    }

    private static int runCounter = 0;

    /**
     * This is the reference that holds the last run.
     * <p>
     * The ASCIIDOCTOR plugin invokes the rendering many times (two or three sometimes) even, when the source has not
     * changed. Rendering an AsciiDoc document is not cheap in itself, but Jamal processing AND rendering can be
     * extremely expensive. You can do extensive things programming Jamal.
     * <p>
     * To avoid unneeded processing Jamal is only executed if the source code has been changed since the last
     * execution. If the MD5 signature of the input is the same as the last execution then we just return whatever
     * the return value was during the last execution. There is no reason to store more than one item, therefore
     * this cache is static.
     */
    private static final AtomicReference<ProcessingCache> cache = new AtomicReference<>(new ProcessingCache(null, null, null));

    @Override
    public void process(Document document, PreprocessorReader reader) {
        final var runCounter = JamalPreprocessor.runCounter++;
        final var fileName = reader.getFile();
        /*
         * The plugin is invoked for all asciidoc files. If the file ending is adoc, asciidoc or anything else then
         * there is nothing to do for the Jamal preprocessor.
         */
        if (!fileName.endsWith(".jam")) {
            return;
        }
        final var markdown = fileName.endsWith("md.jam") || fileName.endsWith("markdown.jam");
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
        final var firstLine = lines.size() > 0 ? lines.get(0).trim() : "";
        final var opts = new InFileOptions(firstLine);
        if (opts.off) {
            reader.restoreLines(lines);
            return;
        }

        if (opts.fromFile) {
            try {
                final var fileLines = Files.readAllLines(Path.of(fileName), StandardCharsets.UTF_8);
                // only if the file was read, it skips in the case of exception
                lines.clear();
                lines.addAll(fileLines);
            } catch (IOException e) {
                // just ignore
            }
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
            } else {
                final var result = runJamalInProcess(fileName, lines, opts.useDefaultSeparators, text, cachingFileReader);
                newLines = result.lines;
                try {
                    final var output = MacroReader.macro(result.processor).readValue("asciidoc:output").orElse(null);
                    if (output != null) {
                        final var outputFile = new File(FileTools.absolute(fileName, output));
                        if (outputFile.exists() && outputFile.isDirectory()) {
                            final var outputFileNameFile = new File(outputFileName);
                            outputFileName = new File(outputFile, outputFileNameFile.getName()).getAbsolutePath();
                        } else {
                            //noinspection ResultOfMethodCallIgnored
                            outputFile.mkdirs();
                            outputFileName = outputFile.getAbsolutePath();
                        }
                    }
                } catch (BadSyntax ignored) {
                }
            }
            log.info("setting cache");
            JamalPreprocessor.cache.set(new ProcessingCache(md5, newLines, cachingFileReader));
            if (opts.save) {
                writeOutputFile(outputFileName, log, cachingFileReader, newLines);
            }
        }
        if (markdown) {
            final var asciidocLines = Arrays.asList(
                    convertMarkdownToAsciiDoc(String.join("\n", newLines))
                            .split("\n"));
            restoreTheLinesIntoThePlugin(reader, fileName, log, asciidocLines, opts);
        } else {
            restoreTheLinesIntoThePlugin(reader, fileName, log, newLines, opts);
        }
        log.info("DONE");
    }

    private Result runJamalInProcess(final String fileName, final List<String> lines, final boolean useDefaultSeparators, final String text, final CachingFileReader cachingFileReader) {
        final var processor = useDefaultSeparators ? new Processor() : new Processor(Configuration.INSTANCE.macroOpen, Configuration.INSTANCE.macroClose);
        processor.setFileReader(cachingFileReader);
        final var input = Input.makeInput(text, new Position(fileName, 0, 0));
        final var r = process(processor, input);
        r.processor = processor;
        r.lines = postProcess(lines, r, fileName);
        return r;
    }

    private void writeOutputFile(final String outputFileName, final Log log, final CachingFileReader cachingFileReader, final List<String> newLines) {
        try (final var writer = new BufferedWriter(new FileWriter(outputFileName))) {
            for (String newLine : newLines) {
                writer.write(newLine + "\n");
            }
        } catch (Exception e) {
            // there is not much we can do here
        }
        log.info("saved");
        log.info("dependencies\n" + cachingFileReader.list());
    }

    private void restoreTheLinesIntoThePlugin(final PreprocessorReader reader, final String fileName, final Log log, final List<String> lines, final InFileOptions opts) {
        /*
         * when the input is not asciidoc then we add and asciidoc prelude to display the text as source code,
         * but the prelude and also the closing line does not get into the output
         *
         * When the extension is adoc.jam then it is already asciidoc
         * When the extension is md.jam or markdown.jam them it is converted to asciidoc for the display: no prelude, postlude
         */
        if (fileName.endsWith(".adoc.jam") || fileName.endsWith(".md.jam") ||fileName.endsWith(".markdown.jam")) {
            log.info("not adding prelude and post lude, it is an asccidoc file");
            if (opts.keepFrontMatter || !lines.get(0).equals("---")) {
                log.info("Keeping the front matter, or no front matter");
                reader.restoreLines(lines);
            } else {
                final var firstLine = lineIndexAfterTheFrontMatter(lines);
                for (int i = lines.size() - 1; i >= firstLine; i--) {
                    reader.restoreLine(lines.get(i));
                }
            }
        } else {
            log.info("adding pre and post ludes");
            final var sourcedLines = new ArrayList<String>();
            sourcedLines.add("[source]");
            sourcedLines.add("----");
            for (final var line : lines) {
                // add an invisible space that will fool asciidoctor not to end the source block
                if (line.trim().equals("----")) {
                    sourcedLines.add(line.replaceAll("----", "----\u200F\u200F\u200E \u200E"));
                } else {
                    sourcedLines.add(line);
                }
            }
            sourcedLines.add("----");
            reader.restoreLines(sourcedLines);
        }
    }

    /**
     * Search for the end of the front-matter.
     * Front-matter is the part at the start of the asciidoc file that starts with, and ends with a {@code ---} line.
     * It is used by Jekyll and some other site builder tools, and it is ignored by asciidoc.
     * The Jamal plugin puts this front-matter back at the start of the file before processing.
     * In case there is any front-matter after the Jamal processing it is removed so that Asciidoc processing gets the
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
        if (r.errorMessage == null && r.result != null) {
            return List.of(r.result.split("\n"));
        } else {
            List<String> newLines = new ArrayList<>();

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

    private JamalPreprocessor.Result process(final Processor processor, final Input input) {
        final Result r = new Result();
        try {
            r.result = processor.process(input);
        } catch (BadSyntaxAt bs) {
            r.position = bs.getPosition();
            r.errorMessage = bs.getMessage();
            r.exception = bs;
        } catch (Exception bs) {
            r.position = new Position("", 0);
            r.errorMessage = bs.getMessage();
            r.exception = bs;
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
}