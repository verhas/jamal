package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class JShellMacro implements Macro {

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        processor.getJShellEngine().define(input.toString());
        return "";
    }

    @Override
    public String getId() {
        return "JShell";
    }
}

