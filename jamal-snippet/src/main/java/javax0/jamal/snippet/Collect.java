package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.MacroReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scan a file or the directory tree and collect the snippets from the files.
 */
public class Collect implements Macro, InnerScopeDependent {
    private enum State {IN, OUT}

    private static final String IMPOSSIBLE_TO_MATCH = "a^";
    private static final String EVERYTHING_MATCHES = ".*";

    @Override
    public String getId() {
        return "snip:collect";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        final var reference = in.getReference();
        var from = FileTools.absolute(reference, in.toString().trim());

        final var reader = MacroReader.macro(processor);
        final var include = Pattern.compile(reader.readValue("include").orElse(EVERYTHING_MATCHES)).asPredicate();
        final var exclude = Pattern.compile(reader.readValue("exclude").orElse(IMPOSSIBLE_TO_MATCH)).asPredicate().negate();
        final var start = Pattern.compile(reader.readValue("start").orElse("snippet\\s+([a-zA-Z0-9_$]+)"));
        final var stop = Pattern.compile(reader.readValue("stop").orElse("end\\s+snippet"));

        final var store = SnippetStore.getInstance(processor);
        if (new File(from).isFile()) {
            harvestSnippets(from, store, start, stop);
        } else {
            try {
                for (final var file : files(from).map(p -> p.toAbsolutePath().toString())
                    .filter(include).filter(exclude).collect(Collectors.toSet())) {
                    harvestSnippets(file, store, start, stop);
                }
            } catch (IOException e) {
                throw new BadSyntax("There is some problem collecting snippets from files under '" + from + "'", e);
            }
        }
        return "";
    }

    private void harvestSnippets(String file, SnippetStore store, Pattern start, Pattern stop) throws BadSyntax {
        var state = State.OUT;
        String id = "";
        StringBuilder text = new StringBuilder();
        int startLine = 0;
        try (final var br = new BufferedReader(new FileReader(file))) {
            int lineNr = 0;
            for (String line; (line = br.readLine()) != null; ) {
                lineNr++;
                switch (state) {
                    case OUT:
                        final var startMatcher = start.matcher(line);
                        if (startMatcher.find()) {
                            id = startMatcher.group(1);
                            text.delete(0, text.length());
                            startLine = lineNr;
                            state = State.IN;
                            break;
                        } else {
                            break;
                        }
                    case IN:
                        final var stopMatcher = stop.matcher(line);
                        if (stopMatcher.find()) {
                            store.snippet(id, text.toString(), new Position(file, startLine));
                            state = State.OUT;
                            break;
                        }
                        text.append(line).append("\n");
                        break;
                }
            }
        } catch (IOException e) {
            throw new BadSyntax("Cannot read the file '" + file + "'");
        }
    }

    /**
     * <p>Get all files in a directory recursively visiting subdirectories.</p>
     *
     * @param dir the root directory where the collection of the files starts
     * @return get the stream of regular files in and under the directory with no directory depth limitation
     * @throws IOException in case there is some problem with the file system
     */
    private static Stream<Path> files(final String dir) throws IOException {
        return Files.find(Paths.get(dir),
            Integer.MAX_VALUE,
            (filePath, fileAttr) -> fileAttr.isRegularFile()
        );
    }
}
