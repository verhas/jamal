package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.NamedMarker;

/**
 * End a scope started with the macro {@link Begin}. The input of the macro is used to identify the scope and
 * it has to be the same, which was used in the macro {@link Begin}.
 */
public class End implements Macro {

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var position = input.getPosition();
        var marker = new NamedMarker(input.toString().trim(), s -> "{@end " + s + "}", position);
        processor.getRegister().pop(marker);
        return "";
    }
}
