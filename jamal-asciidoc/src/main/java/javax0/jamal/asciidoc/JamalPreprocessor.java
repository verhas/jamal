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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class JamalPreprocessor extends Preprocessor implements ExtensionRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {
        JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        javaExtensionRegistry.preprocessor(JamalPreprocessor.class);
    }

    @Override
    public void process(Document document, PreprocessorReader reader) {

        final var fileName = reader.getFile();
        if (!fileName.endsWith(".jam")) {
            return;
        }
        final var lines = reader.readLines();
        final var in = lines.get(0).trim();
        final var matcher = Pattern.compile("@comment\\s+([\\w\\s\\d]*)").matcher(in);
        // save the converted text from `xxx.adoc.jam` --> `xxx.adoc` by default
        boolean save = true;
        // by default, we do not write log file
        boolean log = false;
        if (matcher.find()) {
            final var options = List.of(matcher.group(1).split("\\s+"));
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
        }
        final var useDefaultSeparators = in.length() > 1 && in.charAt(0) == SpecialCharacters.IMPORT_SHEBANG1 && in.charAt(1) == SpecialCharacters.IMPORT_SHEBANG2;
        final var processor = useDefaultSeparators ? new Processor() : new Processor("{%", "%}");
        final var input = Input.makeInput(String.join("\n", lines), new Position(fileName, 0, 0));
        String result = null;
        Position position = new Position("", 0, 0);
        String errorMessage = null;
        Exception exception = null;
        try {
            result = processor.process(input);
        } catch (BadSyntaxAt bs) {
            position = bs.getPosition();
            errorMessage = bs.getMessage();
            exception = bs;
        } catch (Exception bs) {
            position = new Position("", 0);
            errorMessage = bs.getMessage();
            exception = bs;
        }

        final List<String> newLines;
        if (errorMessage == null && result != null) {
            newLines = List.of(result.split("\n"));
        } else {
            newLines = new ArrayList<>();
            appendError(errorMessage, newLines);
            int i;
            for (i = 0; i < position.line && i < lines.size(); i++) {
                newLines.add(lines.get(i));
            }
            appendError(errorMessage, newLines);
            for (; i < lines.size(); i++) {
                newLines.add(lines.get(i));
            }
            appendError(errorMessage, newLines);
            if (exception != null) {
                newLines.add("[source]");
                newLines.add("----");
                newLines.addAll(List.of(ExceptionDumper.dump(exception).toString().split("\n")));
                newLines.add("----");
            }
        }
        if (save) {
            final var outputFileName = fileName.substring(0, fileName.length() - 4);
            final var outputFile = new File(outputFileName);
            try (final var writer = new BufferedWriter(new FileWriter(outputFile))) {
                for (String newLine : newLines) {
                    writer.write(newLine);
                }
            } catch (Exception e) {
                e.printStackTrace(); // there is not much we can do here
            }
            if (log) {
                final var logFile = new File(outputFileName + ".log");
                try (final var writer = new BufferedWriter(new FileWriter(logFile))) {
                    writer.write("[INFO] " + LocalDateTime.now() + " saved");
                } catch (Exception e) {
                    e.printStackTrace(); // there is not much we can do here
                }
            }
        }
        reader.restoreLines(newLines);
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