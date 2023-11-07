package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SnippetStore implements Identified {
    public static final String SNIPPETS_MACRO_ID = "`snippets";
    private final Map<String, Snippet> snippets = new LinkedHashMap<>();
    private final Set<String> collectedNamedSnippetSets = new HashSet<>();

    public static class Snippet {
        final String id;
        final String text;
        final Position pos;
        final BadSyntaxAt exception;

        private Snippet(String id, String text, Position pos, BadSyntaxAt exception) {
            this.id = id;
            this.text = text;
            this.pos = pos;
            this.exception = exception;
        }
    }

    /**
     * Get the snippet store that belongs to this processor.
     * <p>
     *
     * @param processor the processor of which we need the options store
     * @return the snippet store.
     */
    public static SnippetStore getInstance(Processor processor) {
        final var snippetsMacro = processor.getRegister().getUserDefined(SNIPPETS_MACRO_ID);
        if (snippetsMacro.isPresent()) {
            return (SnippetStore) snippetsMacro.get();
        } else {
            final var newSnippetStore = new SnippetStore();
            processor.defineGlobal(newSnippetStore);
            return newSnippetStore;
        }
    }

    /**
     * Get the list of all the snippets that have an id that matches the {@code idRegex} and come from a file that the
     * name matches the {@code fnRegex} and the content matches {@code textRegex}.
     * <p>
     * Any of the parameters can be {@code null}. In that case no filtering will be done on that condition. For example
     * {@code snippetList(null,null,null)} will list all snippets.
     *
     * @param idRegex   regular expression to match against the name
     * @param fnRegex   regular expression to match against the name of the file
     * @param textRegex regular expression to match against the lines of the snippet content
     * @return the stream of the snippets, possibly an empty stream
     */
    public Stream<Snippet> snippetList(String idRegex, String fnRegex, String textRegex) {
        final Predicate<String> idTest = convertRegex(idRegex);
        final Predicate<String> fnTest = convertRegex(fnRegex);
        final Predicate<String> textTest = convertRegex(textRegex);
        final Predicate<Snippet> snTest = s -> idTest.test(s.id) && fnTest.test(s.pos.file) &&
                Arrays.stream(s.text.split("\n", -1)).anyMatch(textTest);
        return snippets.values().stream().filter(snTest);
    }

    private static Predicate<String> convertRegex(String regex) {
        return regex == null || regex.isEmpty() ? x -> true : Pattern.compile(regex).asPredicate();
    }


    /**
     * The name of the macro is {@code `snippets} that starts with a backtick. This is a character that is not allowed
     * in a macro name. This way the macro instances will be stored in the macro register when it gets registered
     * programmatically, but the macro source cannot reference it and also the built-in macro {@code define} will not
     * overwrite it.
     *
     * @return the constant string {@code `snippets}
     */
    @Override
    public String getId() {
        return SNIPPETS_MACRO_ID;
    }


    /**
     * Add a new snippet to the snippet store.
     *
     * @param id      the identifier (name) of the snippet
     * @param snippet the snippet
     * @param pos     is the position of the snippet, used for error reporting in case a snippet is defined twice
     * @throws BadSyntax when a snippet is redefined
     */
    public void snippet(String id, String snippet, Position pos) throws BadSyntax {
        snippet(id, snippet, pos, null);
    }

    /**
     * Add a new snippet to the snippet store.
     * <p>
     * The snippet store takes into account the fact that ill-formed snippets can be found in some source files. The
     * identification of the snippet start and snippet end is very liberal in order to let different source files to
     * have a snippet start and end lines with different type of comment lines. Java has one liner comments, XML has
     * other tye of comments. The collection process thinks that a snippet starts if there is a line that contains the
     * word {@code snippet} literally and there is an identifier after it. The end of the snippet is recognized by any
     * line that contains the words {@code end} and {@code snippet} with one or more spaces between them.
     * <p>
     * Because of this liberal approach it may happen, as it happens even in this very JavaDoc comment that the
     * collection process finds a snippet start, but does not find the end of a snippet. This should not be a problem.
     * Jamal snippet handler should handle this case, and it does it.
     * <p>
     * When a snippet is found but there is some error during the collection (a.k.a. there was no 'end snippet' till the
     * end of the file) then the collection process also stores the snippet along with an exception. Using such snippet
     * will throw an exception using the exception from the collection as a cause.
     * <p>
     * If there was a collected snippet, but it had an error, and the snippet we want to define now does not have an
     * exception attached to it, then just replace the old erroneous.
     * <p>
     * If there was a collected snippet, but it had an error, and the snippet we want to define now is also erroneous,
     * then we replace the old one with the new adding the exception of the old one to the current one as suppressed
     * exception.
     * <p>
     * If there was a correct snippet with the given id already defined and the actual snippet is also okay, then a
     * snippet is double defined. In this case the method throws an exception.
     *
     * @param id                  the identifier (name) of the snippet
     * @param snippet             the snippet
     * @param pos                 is the position of the snippet, used for error reporting in case a snippet is defined
     *                            twice
     * @param collectionException is either {@code null} or an exception that was created during the collection of the
     *                            snippet. This exception will only be used as a "cause" if the snippet is to be used.
     * @throws BadSyntax when a snippet is redefined
     */
    public void snippet(String id, String snippet, Position pos, BadSyntaxAt collectionException) throws BadSyntax {
        if (snippets.containsKey(id)) {
            if (snippets.get(id).exception == null && collectionException == null) {
                final var snip = snippets.get(id);
                throw new BadSyntax(String.format("Snippet '%s' is already defined originally at %s:%s and again at %s:%s", id, snip.pos.file, snip.pos.line, pos.file, pos.line));
            } else if (snippets.get(id).exception != null && collectionException == null) {
                snippets.put(id, new Snippet(id, snippet, pos, null));
            } else if (snippets.get(id).exception != null && collectionException != null) {
                collectionException.addSuppressed(snippets.get(id).exception);
                snippets.put(id, new Snippet(id, snippet, pos, collectionException));
            }
        } else {
            snippets.put(id, new Snippet(id, snippet, pos, collectionException));
        }
    }

    /**
     * Get the identified snippet's text.
     *
     * @param id the identifier of the snippet needed
     * @return the snippet
     * @throws BadSyntax when a snippet is not defined
     */
    public String snippet(final String id) throws BadSyntax {
        return fetchSnippet(id).text;
    }

    /**
     * Get the content of all the snippets with names matching the pattern.
     * The result will be the text of the snippets appended after each other in the alphabetical order of the names.
     * <p>
     * The typical use is when the snippets contain documentation and are named like {@code nameNNNN} where {@code NNNN} is a
     * number. In this case the pattern {@code name\\d+} will match all the snippets with names starting with {@code name}
     * and followed by one or more digits.
     *
     * @param pattern the pattern to match the snippet names
     * @return the text of the snippets or empty string if there is no snippet matching the pattern. It is not an error
     * when there is no snippet with matching name.
     */
    public String snippet(final Pattern pattern) {
        final var snipSet = new TreeSet<String>();
        for (final var entry : snippets.entrySet()) {
            if (pattern.matcher(entry.getKey()).find()) {
                snipSet.add(entry.getKey());
            }
        }
        final var sb = new StringBuilder();
        for (final var snip : snipSet) {
            sb.append(snippets.get(snip).text);
        }
        return sb.toString();
    }

    public int line(final String id) throws BadSyntax {
        return fetchSnippet(id).pos.line;
    }

    public String file(final String id) throws BadSyntax {
        return fetchSnippet(id).pos.file;
    }

    public Snippet fetchSnippet(final String id) throws BadSyntax {
        BadSyntax.when(!snippets.containsKey(id), "Snippet '%s' is not defined", id);
        final var snippet = snippets.get(id);
        if (snippet.exception != null) {
            throw new BadSyntax("There was an exception during the collection of the snippet '" + id + "'", snippet.exception);
        }
        return snippet;
    }

    /**
     * Tests if the named collection set was already collected or not. In case it was already collected then it will
     * return {@code true}. If it was not collected then it will return {@code false}, but also remembers that this name
     * was already used for collection and next time it will already return {@code true}.
     * <p>
     * This method does not check that the collection was really done. The caller has to do the collection after calling
     * this method and should call this method only once.
     * <p>
     * If the name is null then it just returns false, as it means that the collection is not named and should not be
     * remembered.
     *
     * @param name of the collection, usually given as a parameter to the {@code snip:collect} macro as parameter {@code
     *             onceAs}
     * @return {@code false} the first time for a given name and {@code true} in subsequent calls.
     */
    public boolean testAndSet(String name) {
        if (name != null && collectedNamedSnippetSets.contains(name)) {
            return true;
        } else {
            if (name != null) {
                collectedNamedSnippetSets.add(name);
            }
            return false;
        }
    }

    /**
     * Clear the snippet store deleting all snippets that were collected.
     */
    public void clear() {
        snippets.clear();
        collectedNamedSnippetSets.clear();
    }
}