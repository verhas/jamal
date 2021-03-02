package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.NamedMarker;

public class End implements Macro {

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var position = input.getPosition();
        var marker = new NamedMarker(input.toString().trim(), s -> "{@end " + s + "}", position);
        processor.getRegister().pop(marker);
        return "";
    }
}
