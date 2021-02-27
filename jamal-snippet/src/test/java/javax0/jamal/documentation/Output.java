package javax0.jamal.documentation;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
// snippet Output_java
public class Output implements Macro {
    final Processor localProc = new javax0.jamal.engine.Processor("{", "}");

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        return localProc.process(new javax0.jamal.tools.Input(in.toString(), in.getPosition()));
    }
}
// end snippet