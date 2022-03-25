package javax0.jamal.asciidoc;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.Preprocessor;
import org.asciidoctor.extension.PreprocessorReader;
import org.asciidoctor.jruby.extension.spi.ExtensionRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JamalPreprocessor extends Preprocessor implements ExtensionRegistry {

    @Override
    public void register(Asciidoctor asciidoctor) {
        JavaExtensionRegistry javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
        javaExtensionRegistry.preprocessor(JamalPreprocessor.class);
    }

    @Override
    public void process(Document document, PreprocessorReader reader) {

        final var lines = reader.readLines();
        final var processor = new Processor("{%", "%}");
        final var fileName = reader.getFile();
        if( ! fileName.endsWith(".jam")) {
            reader.restoreLines(lines);
            return;
        }
        final var input = Input.makeInput(String.join("\n", lines), new Position(reader.getFile(), 0, 0));
        String result = null;
        Position position = new Position("", 0, 0);
        String errorMessage = null;
        try {
            result = processor.process(input);
        } catch (BadSyntaxAt bs) {
            position = bs.getPosition();
            errorMessage = bs.getMessage();
        } catch (Exception bs) {
            position = new Position("", 0);
            errorMessage = bs.getMessage();
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


