package javax0.jamal.builtins;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.NamedMarker;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Begin implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) {
        var input = in.getInput();
        skipWhiteSpaces(input);
        var marker = new NamedMarker(input.toString().trim(),s -> "{@begin " + s + "}");
        processor.getRegister().push(marker);
        return "";
    }
}
