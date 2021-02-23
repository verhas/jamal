package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Undefine implements Macro {

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);

        final var id = fetchId(input);
        final var convertedId = convertGlobal(id);
        if (isGlobalMacro(id)) {
            processor.defineGlobal(() -> convertedId);
        } else {
            processor.define(() -> convertedId);
        }
        return "";
    }
}
