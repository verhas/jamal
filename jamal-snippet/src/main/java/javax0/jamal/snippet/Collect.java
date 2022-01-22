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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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
        // snippet collect_options
        final var include = Params.<Predicate<String>>holder("include").orElse(EVERYTHING_MATCHES).as(s -> Pattern.compile(s).asPredicate());
        // can define a regular expression. Only those files will be collected that match partially the regular expression.
        final var exclude = Params.<Predicate<String>>holder("exclude").orElse(IMPOSSIBLE_TO_MATCH).as(s -> Pattern.compile(s).asPredicate().negate());
        // can define a regular expression. Only those files will be collected that do not match partially the regular expression.
        // For example, the test file
        //
        //[source]
        //----
        //    {%@include ./src/test/resources/javax0/jamal/snippet/test3.jam%}
        //----
        //
        //excludes any file that contains the character `2` in its name.
        //
        final var start = Params.<Pattern>holder("start").orElse("snippet\\s+([a-zA-Z0-9_$]+)").asPattern();
        // can define a regular expression. The lines that match the regular expression will signal the start of a snippet.
        final var liner = Params.<Pattern>holder("liner").orElse("snipline\\s+([a-zA-Z0-9_$]+)").asPattern();
        // can define a regular expression. The lines that match the regular expression will signal the start of a one liner snippet.
        final var stop = Params.<Pattern>holder("stop").orElse("end\\s+snippet").asPattern();
        // can define a regular expression. The lines that match the regular expression will signal the end of a snippet.
        final var scanDepth = Params.holder("scanDepth").orElseInt(Integer.MAX_VALUE);
        // can limit the directory traversing to a certain depth.
        final var from = Params.<String>holder("from").as(s -> FileTools.absolute(reference, s));
        // can specify the start directory for the traversing.
        final var setName = Params.<String>holder(null, "onceAs").orElseNull();
        // You can use the parameter `onceAs` to avoid repeated snippet collections.
        // Your collect macro may be in an included file, or the complexity of the structure of the Jamal source is complex.
        // At a certain point, it may happen that Jamal already collected the snippets you need.
        // Collecting it again would be erroneous.
        // When snippets are collected, you cannot redefine a snippet.
        // If you define a parameter as `onceAs="the Java samples from HPC"` then the collect macro will remember this name.
        // If you try to collect anything with the same `onceAs` parameter, the collection will ignore it.
        // It was already collected.

        // end snippet
        Params.using(processor).from(this)
            .tillEnd().keys(include, exclude, start, liner, stop, from, scanDepth, setName).parse(in);

        final var store = SnippetStore.getInstance(processor);
        if (store.testAndSet(setName.get())) {
            return "";
        }
        final var fn = from.get();
        final var fromFile = new File(fn);
        if (FileTools.isRemote(fn) || fromFile.isFile()) {
            harvestSnippets(fn, store, start.get(), liner.get(), stop.get());
        } else {
            try {
                final var selectedFiles = files(fn, scanDepth.get())
                    .map(p -> p.toAbsolutePath().toString())
                    .filter(include.get())
                    .filter(exclude.get())
                    .collect(Collectors.toSet());
                for (final var file : selectedFiles) {
                    harvestSnippets(Paths.get(new File(file).toURI()).normalize().toString(), store, start.get(), liner.get(), stop.get());
                }
            } catch (IOException | UncheckedIOException e) {
                throw new BadSyntax("There is some problem collecting snippets from files under '" + from.get() + "'", e);
            }
        }
        return "";
    }

    private void harvestSnippets(String file, SnippetStore store, Pattern start, Pattern liner, Pattern stop) throws BadSyntax {
        var state = State.OUT;
        String id = "";
        StringBuilder text = new StringBuilder();
        final var lines = FileTools.getFileContent(file).split("\n", -1);
        int startLine = 0;
        for (int lineNr = 0; lineNr < lines.length; lineNr++) {
            String line = lines[lineNr];
            switch (state) {
                case OUT:
                    final var startMatcher = start.matcher(line);
                    final var linerMatcher = liner.matcher(line);
                    if (startMatcher.find()) {
                        id = startMatcher.group(1);
                        text.delete(0, text.length());
                        startLine = lineNr;
                        state = State.IN;
                    } else if (linerMatcher.find()) {
                        id = linerMatcher.group(1);
                        text.delete(0, text.length());
                        if (lineNr == lines.length - 1) {
                            throw new BadSyntax("'snipline " + id + "' is on the last line of the file '" + file + "'");
                        }
                        line = lines[++lineNr];
                        store.snippet(id, line, new Position(file, lineNr));
                    }
                    break;
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
        return Files.find(Paths.get(dir),
            scanDepth,
            (filePath, fileAttr) -> fileAttr.isRegularFile()
        );
    }
}
