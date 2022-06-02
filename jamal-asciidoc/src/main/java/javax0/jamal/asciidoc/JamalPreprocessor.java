package javax0.jamal.asciidoc;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;
import javax0.jamal.api.SpecialCharacters;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class JamalPreprocessor extends Preprocessor implements ExtensionRegistry {
    /**
     * The result structure of the execution of in-process Jamal. This is needed to pass the result in a single return value from a method
     */
    private static class Result {
        String result = null;
        Position position = new Position("", 0, 0);
        String errorMessage = null;
        Exception exception = null;
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
     * the return value was during the last execution. There is no reason to store more than one item.
     */
    private static final AtomicReference<ProcessingCache> cache = new AtomicReference<>(new ProcessingCache(null, null, null));

    @Override
    public void process(Document document, PreprocessorReader reader) {
        final var runCounter = JamalPreprocessor.runCounter++;
        final var fileName = reader.getFile();
        if (!fileName.endsWith(".jam")) {
            return;
        }
        final var startTime = LocalDateTime.now();
        final var lines = reader.readLines();
        final var in = lines.size() > 0 ? lines.get(0).trim() : "";
        final var matcher = Pattern.compile("@comment\\s+([\\w\\s\\d]*)").matcher(in);
        // save the converted text from `xxx.adoc.jam` --> `xxx.adoc` by default
        boolean save = !Configuration.INSTANCE.nosave;
        // by default, we do not write log file
        boolean log = Configuration.INSTANCE.log;
        boolean external = Configuration.INSTANCE.external;
        boolean noDependencies = Configuration.INSTANCE.noDependencies;
        if (matcher.find()) {
            final var options = List.of(matcher.group(1).split("\\s+"));
            // snippet OPTIONS
            if (options.contains("off")) {
                reader.restoreLines(lines);
                return;
            }
            if (options.contains("nosave")) {
                save = false;
            }
            if (options.contains("log")) {
                log = true;
            }
            if (options.contains("external")) {
                external = true;
            }
            if (options.contains("noDependencies")) {
                noDependencies = true;
            }
            // end snippet
        }
        final var outputFileName = fileName.substring(0, fileName.length() - 4);
        logInfo(log, outputFileName, "started", runCounter, startTime);
        final var useDefaultSeparators = in.length() > 1 && in.charAt(0) == SpecialCharacters.IMPORT_SHEBANG1 && in.charAt(1) == SpecialCharacters.IMPORT_SHEBANG2;
        final var text = String.join("\n", lines);
        final String md5 = Md5Calculator.md5(text);
        logInfo(log, outputFileName, "md5 " + md5, runCounter, LocalDateTime.now());
        final var myCache = cache.get();
        final var cachingFileReader = new CachingFileReader(noDependencies);
        final List<String> newLines;
        if (myCache.isTheSame(md5)) {
            newLines = myCache.newLines;
            cachingFileReader.readFiles.putAll(myCache.readFiles);
            logInfo(log, outputFileName, "restored", runCounter, LocalDateTime.now());
        } else {
            if (external) {
                newLines = JamalExecutor.execute(fileName, lines);
            } else {
                final var processor = useDefaultSeparators ? new Processor() : new Processor(Configuration.INSTANCE.macroOpen, Configuration.INSTANCE.macroClose);
                processor.setFileReader(cachingFileReader);
                final var input = Input.makeInput(text, new Position(fileName, 0, 0));
                final var r = process(processor, input);
                newLines = postProcess(lines, r);
            }
        }

        if (save) {
            final var outputFile = new File(outputFileName);
            try (final var writer = new BufferedWriter(new FileWriter(outputFile))) {
                for (String newLine : newLines) {
                    writer.write(newLine + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace(); // there is not much we can do here
            }
            if (log) {
                logInfo(outputFileName, "saved", runCounter, LocalDateTime.now());
                logInfo(outputFileName, "dependencies\n" + cachingFileReader.list(), runCounter, LocalDateTime.now());
            }
        }
        cache.set(new ProcessingCache(md5, newLines, cachingFileReader));
        reader.restoreLines(newLines);
    }

    private List<String> postProcess(final List<String> lines, final Result r) {
        List<String> newLines;
        if (r.errorMessage == null && r.result != null) {
            newLines = List.of(r.result.split("\n"));
        } else {
            newLines = new ArrayList<>();
            appendError(r.errorMessage, newLines);
            int i;
            for (i = 0; i < r.position.line && i < lines.size(); i++) {
                newLines.add(lines.get(i));
            }
            appendError(r.errorMessage, newLines);
            for (; i < lines.size(); i++) {
                newLines.add(lines.get(i));
            }
            appendError(r.errorMessage, newLines);
            if (r.exception != null) {
                newLines.add("[source]");
                newLines.add("----");
                newLines.addAll(List.of(ExceptionDumper.dump(r.exception).toString().split("\n")));
                newLines.add("----");
            }
        }
        return newLines;
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

    private void logInfo(final String outputFileName, final String message, final int instance, final LocalDateTime when) {
        logInfo(true, outputFileName, message, instance, when);
    }

    private void logInfo(final boolean log, final String outputFileName, final String message, final int instance, final LocalDateTime when) {
        if (log) {
            try {
                Files.writeString(Paths.get(outputFileName + ".log"),
                        String.format("%s [%d:%d:%s:%08X] %s\n",
                                when,
                                instance,
                                Thread.currentThread().getId(),
                                Thread.currentThread().getName(),
                                this.hashCode(),
                                message),
                        StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } catch (Exception e) {
                e.printStackTrace(); // there is not much we can do here
            }
        }
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