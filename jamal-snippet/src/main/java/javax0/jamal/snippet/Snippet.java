package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;
import javax0.jamal.tools.Format;

public class Snippet implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var pos = in.getPosition();
        skipWhiteSpaces(in);
        final var id = fetchId(in);
        skipWhiteSpaces(in);
        BadSyntax.when(!firstCharIs(in, '='), Format.msg("snippet '%s' has no '=' to body", id));
        skip(in, 1);
        skipWhiteSpaces2EOL(in);
        SnippetStore.getInstance(processor).snippet(id, in.toString(), pos);
        return "";
    }

    @Override
    public String getId() {
        return "snip:define";
    }
}
