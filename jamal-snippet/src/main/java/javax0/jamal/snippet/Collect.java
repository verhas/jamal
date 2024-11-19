package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;
import javax0.javalex.JavaLexed;
import javax0.javalex.MatchResult;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scan a file or the directory tree and collect the snippets from the files.
 */
public class Collect implements Macro, InnerScopeDependent, Scanner.WholeInput {
    private enum State {IN, OUT}

    private static final String IMPOSSIBLE_TO_MATCH = "a^";
    private static final String EVERYTHING_MATCHES = ".*";

    @Override
    public String getId() {
        return "snip:collect";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        //<editor-fold desc="Collection Options" default="collapsed">
        final var pos = in.getPosition();
        final var scanner = newScanner(in, processor);
        // snippet collect_options
        final var include = scanner.<Predicate<String>>param("include").defaultValue(EVERYTHING_MATCHES).as(s -> Pattern.compile(s).asPredicate());
        // can define a regular expression. Only those files will be collected that match partially the regular expression.
        final var exclude = scanner.<Predicate<String>>param("exclude").defaultValue(IMPOSSIBLE_TO_MATCH).as(s -> Pattern.compile(s).asPredicate().negate());
        // can define a regular expression. Only those files will be collected that do not match partially the regular expression.
        // For example, the test file
        // +
        //[source]
        //----
        //    {%@include ../../../../../../src/test/resources/javax0/jamal/snippet/test3.jam%}
        //----
        // +
        //excludes any file that contains the character `2` in its name.
        //
        final var start = scanner.pattern("start").defaultValue("snippet\\s+([a-zA-Z0-9_$]+)");
        // can define a regular expression. The lines that match the regular expression will signal the start of a snippet.
        final var liner = scanner.pattern("liner").defaultValue("snipline\\s+([a-zA-Z0-9_$]+)");
        // can define a regular expression. The lines that match the regular expression will signal the start of a one-liner snippet.
        final var lineFilter = scanner.<Predicate<String>>param("lineFilter", "filter").defaultValue("filter=(.*)").asPattern();
        // can define a regular expression. The pattern will be used against any 'snipline' lines to find the regular expression that will be used to filter the content of the line
        final var stop = scanner.pattern("stop").defaultValue("end\\s+snippet");
        // can define a regular expression. The lines that match the regular expression will signal the end of a snippet.
        final var scanDepth = scanner.number("scanDepth").defaultValue(Integer.MAX_VALUE);
        // can limit the directory traversing to a certain depth.
        final var from = scanner.file("from");
        // can specify the start directory for the traversing.
        final var setName = scanner.str(null, "onceAs").optional();
        // You can use the parameter `onceAs` to avoid repeated snippet collections.
        // Your collect macro may be in an included file, or the Jamal source structure is complex.
        // At a certain point, it may happen that Jamal already collected the snippets you need.
        // Collecting it again would be erroneous.
        // When snippets are collected, you cannot redefine a snippet.
        // If you define a parameter as `onceAs="the Java samples from HPC"` then the collect macro will remember this name.
        // If you try to collect anything with the same `onceAs` parameter, the collection will ignore it.
        // It was already collected.
        final var prefix = scanner.str("prefix").defaultValue("");
        // You can define a prefix, which is prepended to the snippet names.
        // The snippets will be stored with this prefix, and the macros should use these prefixed names to reference the snippets.
        // For example, if you define the prefix as `myprefix::` then the snippet named `mysnippet` will be stored as `myprefix::mysnippet`.
        final var postfix = scanner.str("postfix").defaultValue("");
        // You can define a postfix, which is appended to the snippet names.
        // The snippets will be stored with this postfix, and the macros should use these postfixed names to reference the snippets.
        // For example, if you define the postfix as `::mypostfix` then the snippet named `mysnippet` will be stored as `mysnippet::mypostfix`.
        //
        //+
        // The parameter `prefix` and `postfix` can be used together.
        // The use case is when you collect snippets from different sources where the names may collide.
        final var java = scanner.bool(null, "java");
        // Collect snippets from the Java sources based on the Java syntax without any special tag.
        final var javaSnippetCollectors = scanner.str("javaSnippetCollectors").optional();
        // You can define a comma-separated list of Java snip{%@comment%}pet collectors.
        final var asciidoc = scanner.bool("asciidoc", "asciidoctor");
        // Using this parameter, the macro will collect snippets using the ASCIIDOC tag syntax.
        // This syntax starts a snippet with `tag::name[]` and ends it with `end::name[]`, where `name` is the name of the snippet.
        // Using these start and stop delimiters, the snippets can also be nested arbitrarily, and they can also overlap.
        final var ignoreIOEx = scanner.bool("ignoreErrors");
        // Using this parameter, the macro will ignore IOExceptions.
        // An IOException typically occurs when a file is binary and by accident it contains an invalid UTF-8 sequence.
        // Use this option only as a last resort.
        // Better do not mix binary files with ASCII files.
        // Even if there are binary files from where you collect snippets from ASCII files, use the option `exclude` to exclude the binaries.
        // end snippet
        scanner.done();
        //</editor-fold>

        BadSyntax.when(asciidoc.is() && java.is(), "You cannot use both 'asciidoc' and 'java' parameters in the same collect macro.");

        final var store = SnippetStore.getInstance(processor);
        if (store.testAndSet(setName.get())) {
            return "";
        }
        final var fn = from.get();
        final String normFn = from.isRemote() ? from.get() : Paths.get(from.file().toURI()).normalize().toString();

        if (from.isRemote() || from.isFile()) {
            if (asciidoc.is()) {
                harvestAsciiDoc(normFn, store, pos, prefix.get(), postfix.get(), ignoreIOEx.is(), processor);
            } else if (java.is()) {
                harvestJava(normFn, store, pos, ignoreIOEx.is(), prefix.get(), postfix.get(), javaSnippetCollectors.get(), processor);
            } else {
                harvestSnippets(normFn, store, start.get(), liner.get(), lineFilter.get(), stop.get(), pos, prefix.get(), postfix.get(), ignoreIOEx.is(), processor);
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
                                ignoreIOEx.is(),
                                processor);
                    } else if (java.is()) {
                        harvestJava(Paths.get(new File(file).toURI()).normalize().toString(),
                                store,
                                pos,
                                ignoreIOEx.is(), prefix.get(), postfix.get(),
                                javaSnippetCollectors.get(),
                                processor);
                    } else {
                        harvestSnippets(Paths.get(new File(file).toURI()).normalize().toString(),
                                store,
                                start.get(),
                                liner.get(),
                                lineFilter.get(),
                                stop.get(),
                                pos,
                                prefix.get(),
                                postfix.get(),
                                ignoreIOEx.is(),
                                processor);
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


    private void harvestJava(final String file,
                             final SnippetStore store,
                             final Position pos,
                             boolean ignoreIOEx,
                             final String prefix,
                             final String postfix,
                             final String javaSnippetCollectors,
                             Processor processor) throws BadSyntax {
        List<BadSyntax> errors = new ArrayList<>();
        BadSyntax.when(javaSnippetCollectors == null || javaSnippetCollectors.isEmpty(), "You must specify at least one Java snip" + "pet collector.");
        final String[] lines = getFileContent(file, ignoreIOEx, processor);
        final JavaLexed javaLexed;
        try {
            javaLexed = new JavaLexed(String.join("\n", lines));
        } catch (Exception e) {
            throw new BadSyntax("There is some problem collecting snippets from file '" + file + "'", e)    ;
        }
        for (final var collector : javaSnippetCollectors.split(",")) {
            if (collector == null || collector.isEmpty()) {
                continue;
            }
            final var matcher = JavaMatcherBuilderMacros.getMatcherFromMacro(processor, collector.trim());
            int i = 0;
            MatchResult result;
            while ((result = javaLexed.find(matcher).fromIndex(i).result()).matches) {
                i = result.end;
                final var idGrp = javaLexed.group("snippetName");
                final var idRgrp = javaLexed.regexGroups("snippetName");
                final String snippetName;
                if (idGrp != null && !idGrp.isEmpty()) {
                    snippetName = idGrp.get(0).getLexeme();
                } else if (idRgrp != null && idRgrp.isPresent() && idRgrp.get().groupCount() > 0) {
                    snippetName = idRgrp.get().group(1);
                } else {
                    throw new BadSyntax("Java collector found a matcher that does not have a 'snip' group defining the name of the snippet");
                }

                final var snipGrp = javaLexed.group("snippet");
                final var snipRgrp = javaLexed.regexGroups("snippet");
                final String snippetContent;
                if (snipGrp != null && !snipGrp.isEmpty()) {
                    snippetContent = snipGrp.get(0).getLexeme();
                } else if (snipRgrp != null && snipRgrp.isPresent() && snipRgrp.get().groupCount() > 0) {
                    snippetContent = snipRgrp.get().group(1);
                } else {
                    throw new BadSyntax("Java collector found a matcher that does not have a 'snip' group defining the name of the snippet");
                }
                try {
                    store.snippet(prefix + snippetName + postfix, snippetContent, pos);
                } catch (BadSyntax e) {
                    errors.add(new BadSyntaxAt("Collection error", pos, e));
                }
            }
            assertNoErrors(file, errors);
        }
    }

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
                                 boolean ignoreIOEx,
                                 Processor processor) throws BadSyntax {
        final var openedSnippets = new HashMap<String, SnippetAccumulator>();
        final String[] lines = getFileContent(file, ignoreIOEx, processor);
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
     * @param file       the file to harvest from
     * @param store      the store to store the snippets in
     * @param start      the start pattern
     * @param liner      the one-line pattern that reads a one-line snippet
     * @param lineFilter to filter the line that is being harvested
     * @param stop       the stop pattern
     * @param pos        the position of the file
     * @param prefix     the prefix to add to the snippet id
     * @param postfix    the postfix to add to the snippet id
     * @throws BadSyntax if there is some problem collecting the snippets, snippets are not closed, or are defined more
     *                   than once.
     */
    private void harvestSnippets(final String file,
                                 final SnippetStore store,
                                 final Pattern start,
                                 final Pattern liner,
                                 final Pattern lineFilter,
                                 final Pattern stop,
                                 final Position pos,
                                 final String prefix,
                                 final String postfix,
                                 boolean ignoreIOEx,
                                 Processor processor) throws BadSyntax {
        var state = State.OUT;
        String id = "";
        StringBuilder text = new StringBuilder();
        final String[] lines = getFileContent(file, ignoreIOEx, processor);
        int startLine = 0;
        List<BadSyntax> errors = new ArrayList<>();
        for (int lineNr = 0; lineNr < lines.length; lineNr++) {
            String line = lines[lineNr];
            switch (state) {
                case OUT:
                    final var startMatcher = start.matcher(line);
                    final var linerMatcher = liner.matcher(line);
                    final var filterMatcher = lineFilter.matcher(line);
                    if (startMatcher.find()) {
                        id = startMatcher.group(1);
                        text.setLength(0);
                        startLine = lineNr;
                        state = State.IN;
                    } else if (linerMatcher.find()) {
                        id = linerMatcher.group(1);
                        text.setLength(0);
                        final String id_ = id;
                        BadSyntax.when(lineNr == lines.length - 1, "'snipline %s' is on the last line of the file '%s'", id_, file);
                        line = lines[++lineNr];
                        line = cutOffPartUsingMatcher(line, filterMatcher);
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
     * cut off a part of the line using the filterMatcher.
     * If the filterMatcher finds a match, the part of the line is cut off.
     * Otherwise, the line is returned as it is.
     *
     * @param line          the line to cut off
     * @param filterMatcher the pattern matcher on the line
     * @return the line or a part of the line if the filterMatcher finds a match
     * @throws BadSyntax when the regular expression is malformed or does not have exactly one capturing group
     */
    private String cutOffPartUsingMatcher(String line, final Matcher filterMatcher) throws BadSyntax {
        if (filterMatcher.find()) {
            final var regex = filterMatcher.group(1);
            try {
                final var matcher = Pattern.compile(regex).matcher(line);
                if (matcher.find()) {
                    BadSyntax.when(matcher.groupCount() != 1, "The regex '%s' must have exactly one capturing group", regex);
                    line = matcher.group(1);
                } else {
                    throw new BadSyntax(String.format("The regex '%s' did not match the next line.", regex));
                }
            } catch (PatternSyntaxException pse) {
                throw new BadSyntax(String.format("Invalid regex '%s'", regex));
            }
        }
        return line;
    }

    /**
     * Get the content of a file.
     *
     * @param file       the name of the file to be read
     * @param ignoreIOEx ignore IOException if true. In that case the method returns an empty array.
     * @return the lines of the file
     * @throws BadSyntax if the file cannot be read
     */
    private String[] getFileContent(final String file, final boolean ignoreIOEx, final Processor processor) throws BadSyntax {
        try {
            return FileTools.getFileContent(file, processor).split("\n", -1);
        } catch (BadSyntax e) {
            if (ignoreIOEx) {
                return new String[0];
            } else {
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
/*template jm_collect
{template |collect|snip:collect from=$FROM$ $INCLUDE$ $EXCLUDE$ $LINEFILTER$ $START$ $LINER$ $STOP$ $SCANDEPTH$ $ONCEAS$ $PREFIX$ $POSTFIX$ $JAVASNIPPETCOLLECTOR$ $JAVA$ $ASCIIDOC$ $IGNOREERRORS$|collect snippets|

        {variable |INCLUDE|"include=.*"}
        {variable |EXCLUDE|"exclude=.*"}
        {variable |LINEFILTER|"lineFilter=.*"}
        {variable |START|"start=.*"}
        {variable |LINER|"liner=.*"}
        {variable |STOP|"stop=.*"}
        {variable |SCANDEPTH|"scanDepth=1"}
        {variable |FROM|fileRelativePath()}
        {variable |ONCEAS|"onceAs=..."}
        {variable |PREFIX|"prefix=..."}
        {variable |POSTFIX|"postfix=..."}
        {variable |JAVASNIPPETCOLLECTOR|"javaSnippetCollectors=..."}
        {variable |JAVA|"java"}
        {variable |ASCIIDOC|"asciidoc"}
        {variable |IGNOREERRORS|"ignoreErrors"}
}
*/