package javax0.jamal.doclet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class Code implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        return "<code>" + in + "</code>";
    }
}
