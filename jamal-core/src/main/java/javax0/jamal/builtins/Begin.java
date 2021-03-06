package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.NamedMarker;

public class Begin implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var position = in.getPosition();
        var marker = new NamedMarker(in.toString().trim(), s -> "{@begin " + s + "}", position);
        processor.getRegister().push(marker);
        return "";
    }
}
