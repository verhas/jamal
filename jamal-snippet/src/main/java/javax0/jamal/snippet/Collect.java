package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
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
        final var reference = in.getReference();
        final var include = Params.<Predicate<String>>holder("include").orElse(EVERYTHING_MATCHES).as(s -> Pattern.compile(s).asPredicate());
        final var exclude = Params.<Predicate<String>>holder("exclude").orElse(IMPOSSIBLE_TO_MATCH).as(s -> Pattern.compile(s).asPredicate().negate());
        final var start = Params.<Pattern>holder("start").orElse("snippet\\s+([a-zA-Z0-9_$]+)").as(Pattern::compile);
        final var stop = Params.<Pattern>holder("stop").orElse("end\\s+snippet").as(Pattern::compile);
        final var scanDepth = Params.holder("scanDepth").orElse(Integer.MAX_VALUE).asInt();
        final var from = Params.<String>holder("from").as(s -> FileTools.absolute(reference, s));
        Params.using(processor).from(this)
            .tillEnd().keys(include, exclude, start, stop, from, scanDepth).parse(in);

        final var store = SnippetStore.getInstance(processor);
        final var fromFile = new File(from.get());
        if (fromFile.isFile()) {
            harvestSnippets(Paths.get(fromFile.toURI()).normalize().toString(), store, start.get(), stop.get());
        } else {
            try {
                for (final var file :files(from.get(),scanDepth.get()).map(p -> p.toAbsolutePath().toString())
                    .filter(include.get()).filter(exclude.get()).collect(Collectors.toSet())) {
                    harvestSnippets(Paths.get(new File(file).toURI()).normalize().toString(), store, start.get(), stop.get());
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
            if (state == State.IN) {
                store.snippet(id, "", new Position(file, startLine),
                    new BadSyntaxAt("Snippet '" + id + "' was not terminated in the file with \"end snippet\" ", new Position(file, startLine, 0)));
            }
        } catch (IOException e) {
            throw new BadSyntax("Cannot read the file '" + file + "'");
        }
    }

    /**
     * <p>Get all files in a directory recursively visiting subdirectories.</p>
     *
     * @param dir       the root directory where the collection of the files starts
     * @param scanDepth the depth of recursion
     * @return get the stream of regular files in and under the directory
     * @throws IOException in case there is some problem with the file system
     */
    private static Stream<Path> files(final String dir, int scanDepth) throws IOException {
        return  Files.find(Paths.get(dir),
            scanDepth,
            (filePath, fileAttr) -> fileAttr.isRegularFile()
        );
    }
}
