package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.NamedMarker;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class End implements Macro {

    public static String decorateMarker(String s) {
        return "{@end " + s + "}";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        var input = in.getInput();
        skipWhiteSpaces(input);
        var marker = new NamedMarker(input.toString().trim(), End::decorateMarker);
        processor.getRegister().pop(marker);
        return "";
    }
}
