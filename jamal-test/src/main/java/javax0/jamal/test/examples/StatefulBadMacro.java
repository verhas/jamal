package javax0.jamal.test.examples;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

/**
 * This is just a macro, which has state and is not declared to be stateful.
 * It is used in the test {@code TestCheckState} that state checking really works, and that it can really be switched
 * off using system properties. This maro is not used anywhere.
 */
public class StatefulBadMacro implements Macro {
    private int state;
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        state++;
        return ""+state;
    }
}
