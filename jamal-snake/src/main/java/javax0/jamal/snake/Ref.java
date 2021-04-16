package javax0.jamal.snake;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class Ref implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        return "!!javax0.jamal.api.Ref " + in;
    }

    @Override
    public String getId() {
        return "yaml:ref";
    }
}
