package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Option;
import javax0.jamal.tools.OptionsStore;

public class Catch implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        if (OptionsStore.getInstance(processor).is(Try.CAUGHT_ERROR_OPTION)) {
            final var caught = new Option(Try.CAUGHT_ERROR_OPTION);
            processor.define(caught);
            caught.set(false);
            return processor.process(in);
        } else {
            return "";
        }
    }

    @Override
    public String getId() {
        return "catch";
    }
}
