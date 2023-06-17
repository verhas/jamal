package javax0.jamal.java.testmacros;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class TestMacro implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) {
        return "maci";
    }

    @Override
    public String getId() {
        return "maci";
    }
}
