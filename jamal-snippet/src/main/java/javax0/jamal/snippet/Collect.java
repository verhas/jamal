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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        final var pos = in.getPosition();
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
        final var prefix = Params.<String>holder("prefix").orElse("");
        // You can define a prefix, which is prepended to the snippet names.
        // The snippets will be stored with this prefix and the macros should use these prefixed names to reference the snippets.
        // For example, if you define the prefix as `myprefix::` then the snippet named `mysnippet` will be stored as `myprefix::mysnippet`.
        final var postfix = Params.<String>holder("postfix").orElse("");
        // You can define a postfix, which is appended to the snippet names.
        // The snippets will be stored with this postfix and the macros should use these postfixed names to reference the snippets.
        // For example, if you define the postfix as `::mypostfix` then the snippet named `mysnippet` will be stored as `mysnippet::mypostfix`.
        //
        //+
        // The parameter `prefix` and `postfix` can be used together.
        // The use case is when you collect snippets from different sources where the names may collide.
        final var asciidoc = Params.<Boolean>holder("asciidoc", "asciidoctor").asBoolean();
        // Using this parameter, the macro will collect snippets using the ASCIIDOC tag syntax.
        // This syntax starts a snippet with `tag::name[]` and ends it with `end::name[]`, where `name` is the name of the snippet.
        // Using these start and stop delimiters the snippets can also be nested arbitrarily, and they can also overlap.
        final var ignoreIOEx = Params.<Boolean>holder("ignoreErrors").asBoolean();
        // Using this parameter, the macro will ignore IOExceptions.
        // An IOException typically occur, when a file is binary and by accident it contains an invalid UTF-8 sequence.
        // Use this option only as a last resort.
        // Better do not mix binary files with ASCII files.
        // Even if there are binary files from where you collect snippets from ASCII files, use the option `exclude` to exclude the binaries.
        // end snippet
        Params.using(processor).from(this)
                .tillEnd().keys(include, exclude, start, liner, stop, from, scanDepth, setName, prefix, postfix, asciidoc, ignoreIOEx).parse(in);

        final var store = SnippetStore.getInstance(processor);
        if (store.testAndSet(setName.get())) {
            return "";
        }
        final var fn = from.get();
        final var fromFile = new File(fn);
        if (FileTools.isRemote(fn) || fromFile.isFile()) {
            if (asciidoc.is()) {
                harvestAsciiDoc(fn, store, pos, prefix.get(), postfix.get(), ignoreIOEx.is());
            } else {
                harvestSnippets(fn, store, start.get(), liner.get(), stop.get(), pos, prefix.get(), postfix.get(), ignoreIOEx.is());
            }
        } else {
            try {
                final var selectedFiles = files(fn, scanDepth.get())
                        .map(p -> p.toAbsolutePath().toString())
                        .filter(include.get())
                        .filter(exclude.get())
                        .collect(Collectors.toSet());
                for (final var file : selectedFiles) {
                    if (asciidoc.is()) {
                        harvestAsciiDoc(Paths.get(new File(file).toURI()).normalize().toString(),
                                store,
                                pos,
                                prefix.get(),
                                postfix.get(),
                                ignoreIOEx.is());
                    } else {
                        harvestSnippets(Paths.get(new File(file).toURI()).normalize().toString(),
                                store,
                                start.get(),
                                liner.get(),
                                stop.get(),
                                pos,
                                prefix.get(),
                                postfix.get()
                                , ignoreIOEx.is());
                    }
                }
            } catch (IOException | UncheckedIOException e) {
                throw new BadSyntax("There is some problem collecting snippets from files under '" + from.get() + "'", e);
            }
        }
        return "";
    }

    private static class SnippetAccumulator {
        final StringBuilder sb = new StringBuilder();
        final String id;
        final int startLine;
        boolean isOpen = true;

        private SnippetAccumulator(final String id, final int startLine) {
            this.id = id;
            this.startLine = startLine;
        }

        void close() {
            isOpen = false;
        }

        void open() {
            isOpen = true;
        }
    }

    private static final Pattern ASCIIDOC_START = Pattern.compile("tag::([\\w\\d_$]+)\\[.*?]");
    private static final Pattern ASCIIDOC_STOP = Pattern.compile("end::([\\w\\d_$]+)\\[.*?]");

    /**
     * Harvest snippets from a file where the snippets are defined using the ASCIIDOC tag syntax.
     * The syntax is `tag::name[]` and `end::name[]`, where `name` is the name of the snippet.
     *
     * @param file    the file to harvest snippets from
     * @param store   the store to store the snippets in
     * @param pos     the position to store the snippets at
     * @param prefix  the prefix to use for the snippet names
     * @param postfix the postfix to use for the snippet names
     * @throws BadSyntax when there is an error
     */
    private void harvestAsciiDoc(final String file,
                                 final SnippetStore store,
                                 final Position pos,
                                 final String prefix,
                                 final String postfix,
                                 boolean ignoreIOEx) throws BadSyntax {
        final var openedSnippets = new HashMap<String, SnippetAccumulator>();
        final String[] lines = getFileContent(file, ignoreIOEx);
        List<BadSyntax> errors = new ArrayList<>();
        for (int lineNr = 0; lineNr < lines.length; lineNr++) {
            String line = lines[lineNr];
            final var startMatcher = ASCIIDOC_START.matcher(line);
            final var stopMatcher = ASCIIDOC_STOP.matcher(line);
            if (startMatcher.find()) {
                final var id = startMatcher.group(1);
                if (openedSnippets.containsKey(id) && openedSnippets.get(id).isOpen) {
                    errors.add(new BadSyntax("Snippet '" + id + "' is already opened on line " + openedSnippets.get(id).startLine));
                } else {
                    final var startLine = lineNr;
                    final var sa = openedSnippets.computeIfAbsent(id, _id -> new SnippetAccumulator(id, startLine));
                    sa.open();
                }
            } else if (stopMatcher.find()) {
                final var id = stopMatcher.group(1);
                if (!openedSnippets.containsKey(id) && !openedSnippets.get(id).isOpen) {
                    errors.add(new BadSyntax("Snippet '" + id + "' is not opened"));
                } else {
                    final var sa = openedSnippets.get(id);
                    sa.close();
                }
            } else {
                for (final var sa : openedSnippets.values()) {
                    if (sa.isOpen) {
                        sa.sb.append(line).append("\n");
                    }
                }
            }
        }
        if (!openedSnippets.isEmpty()) {
            for (final var sa : openedSnippets.values()) {
                if (sa.isOpen) {
                    errors.add(new BadSyntax("Snippet '" + sa.id + "' opened on the line " + sa.startLine + " is not closed."));
                }
                try {
                    store.snippet(prefix + sa.id + postfix, sa.sb.toString(), new Position(file, sa.startLine));
                } catch (BadSyntax e) {
                    errors.add(new BadSyntaxAt("Collection error", pos, e));
                }
            }
        }
        assertNoErrors(file, errors);
    }

    /**
     * Harvest snippets from a file.
     *
     * @param file    the file to harvest from
     * @param store   the store to store the snippets in
     * @param start   the start pattern
     * @param liner   the one line pattern that reads a one-line snippet
     * @param stop    the stop pattern
     * @param pos     the position of the file
     * @param prefix  the prefix to add to the snippet id
     * @param postfix the postfix to add to the snippet id
     * @throws BadSyntax if there is some problem collecting the snippets, snippets are not closed, or are defined more
     *                   than once.
     */
    private void harvestSnippets(final String file,
                                 final SnippetStore store,
                                 final Pattern start,
                                 final Pattern liner,
                                 final Pattern stop,
                                 final Position pos,
                                 final String prefix,
                                 final String postfix,
                                 boolean ignoreIOEx) throws BadSyntax {
        var state = State.OUT;
        String id = "";
        StringBuilder text = new StringBuilder();
        final String[] lines = getFileContent(file, ignoreIOEx);
        int startLine = 0;
        List<BadSyntax> errors = new ArrayList<>();
        for (int lineNr = 0; lineNr < lines.length; lineNr++) {
            String line = lines[lineNr];
            switch (state) {
                case OUT:
                    final var startMatcher = start.matcher(line);
                    final var linerMatcher = liner.matcher(line);
                    if (startMatcher.find()) {
                        id = startMatcher.group(1);
                        text.setLength(0);
                        startLine = lineNr;
                        state = State.IN;
                    } else if (linerMatcher.find()) {
                        id = linerMatcher.group(1);
                        text.setLength(0);
                        if (lineNr == lines.length - 1) {
                            throw new BadSyntax("'snipline " + id + "' is on the last line of the file '" + file + "'");
                        }
                        line = lines[++lineNr];
                        try {
                            store.snippet(prefix + id + postfix, line, new Position(file, lineNr));
                        } catch (BadSyntax e) {
                            errors.add(new BadSyntaxAt("Collection error", pos, e));
                        }
                    }
                    break;
                case IN:
                    final var stopMatcher = stop.matcher(line);
                    if (stopMatcher.find()) {
                        try {
                            store.snippet(prefix + id + postfix, text.toString(), new Position(file, startLine + 1));
                        } catch (BadSyntax e) {
                            errors.add(new BadSyntaxAt("Collection error: " + e.getMessage(), pos, e));
                        }
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
        assertNoErrors(file, errors);
    }

    /**
     * Get the content of a file.
     *
     * @param file       the name of the file to be read
     * @param ignoreIOEx ignore IOException if true. In that case the method returns an empty array.
     * @return the lines of the file
     * @throws BadSyntax if the file cannot be read
     */
    private String[] getFileContent(final String file, final boolean ignoreIOEx) throws BadSyntax {
        try {
            return FileTools.getFileContent(file).split("\n", -1);
        }catch (BadSyntax e) {
            if( ignoreIOEx ) {
                return new String[0];
            }else{
                throw e;
            }
        }
    }

    /**
     * If there are no errors the method simply returns.
     *
     * @param file   the file name that was parsed and from which the errors were collected
     * @param errors the errors collected
     * @throws BadSyntax is thrown if there are errors. The exception contains a list of all errors as suppressed
     *                   exceptions.
     */
    private void assertNoErrors(final String file, final List<BadSyntax> errors) throws BadSyntax {
        if (!errors.isEmpty()) {
            if (errors.size() == 1) {
                throw errors.get(0);
            }
            final var bs = new BadSyntax("There are some problems with snippets in file '" + file + "'");
            errors.forEach(bs::addSuppressed);
            throw bs;
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
