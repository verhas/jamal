package javax0.jamal.jamal;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class Output implements Macro {
    /**
     * Instantiating a processor also instantiates all macro classes via the ServiceLoader. Instantiating here the
     * processor would instantiate a new instance of all the macros including this one and that would mean an infinite
     * recursion via the service loader.
     */
    private Processor localProc = null;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        if (localProc == null) {
            localProc = new javax0.jamal.engine.Processor("{", "}");
        }
        return localProc.process(new javax0.jamal.tools.Input(in.toString(), in.getPosition()));
    }
}