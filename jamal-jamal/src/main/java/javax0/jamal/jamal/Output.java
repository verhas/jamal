package javax0.jamal.jamal;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class Output implements Macro {
    Processor localProc = null;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        if (localProc == null) {
            localProc = new javax0.jamal.engine.Processor("{", "}");
        }
        return localProc.process(new javax0.jamal.tools.Input(in.toString(), in.getPosition()));
    }
}