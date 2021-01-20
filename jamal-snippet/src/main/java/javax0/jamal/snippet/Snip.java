package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Snip implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        skipWhiteSpaces(in);
        final String id;
        id = InputHandler.fetchId(in);
        // the rest of the input is ignored
        return SnippetStore.getInstance(processor).snippet(id);
    }
}
