package javax0.jamal.builtins;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Ident implements Macro {
    @Override
    public String evaluate(Input input, Processor processor) {
        skipWhiteSpaces(input);
        return input.toString();
    }
}
