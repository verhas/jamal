package javax0.jamal.builtins;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

/**
 * Just do nothing. The return value is #comment is an empty string.
 */
public class Comment implements Macro {
    @Override
    public String evaluate(Input input, Processor processor) {
        return "";
    }
}
