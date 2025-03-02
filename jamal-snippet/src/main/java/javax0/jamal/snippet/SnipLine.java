package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

@Macro.Name("snip:line")
public
class SnipLine implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        skipWhiteSpaces(in);
        final var id = InputHandler.fetchId(in);
        // the rest of the line is ignored

        return "" + (SnippetStore.getInstance(processor).line(id) + 1);
    }

}
