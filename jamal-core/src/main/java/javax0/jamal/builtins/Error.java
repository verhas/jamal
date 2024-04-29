package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Just do nothing. The return value is #comment is an empty string.
 */
public class Error implements Macro {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);
        throw new BadSyntax(input.toString());
    }
}
