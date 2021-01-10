package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class Snip implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        final var id = in.toString().trim();

        return SnippetStore.getInstance(processor).snippet(id);
    }
}
