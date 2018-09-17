package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class Begin implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        processor.getRegister().push();
        return "";
    }
}
