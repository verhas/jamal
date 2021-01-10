package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;

import java.util.HashMap;
import java.util.Map;

public class SnippetStore implements Identified {
    public static final String SNIPPETS_MACRO_ID = "`snippets";
    private final Map<String, Snippet> snippets = new HashMap<>();

    private static class Snippet {
        final String text;
        final Position pos;

        private Snippet(String text, Position pos) {
            this.text = text;
            this.pos = pos;
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
     * @param pos     is the position of the snippet
     * @throws BadSyntax when a snippet is redefined
     */
    public void snippet(String id, String snippet, Position pos) throws BadSyntax {
        if (snippets.containsKey(id)) {
            final var snip = snippets.get(id);
            throw new BadSyntaxAt("Snippet '" + id + "' is already defined in " + snip.pos.file + ":" + snip.pos.line, pos);
        }
        snippets.put(id, new Snippet(snippet, pos));
    }

    /**
     * Get the identified snippet
     *
     * @return the snippet
     * @throws BadSyntax when a snippet is not defined
     */
    public String snippet(final String id) throws BadSyntax {
        if (!snippets.containsKey(id)) {
            throw new BadSyntax("Snippet '" + id + "' is not defined");
        }
        return snippets.get(id).text;
    }

    /**
     * Clear the snippet store deleting all snippets that were collected.
     */
    public void clear(){
        snippets.clear();
    }
}
