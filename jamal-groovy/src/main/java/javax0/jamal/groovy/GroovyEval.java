package javax0.jamal.groovy;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class GroovyEval implements Macro, InnerScopeDependent {
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        try {
            final var shell = Shell.getShell(processor);
            return "" + shell.evaluate(in.toString(), null);
        } catch (Exception e) {
            throw new BadSyntax("Error evaluating groovy script using eval", e);
        }
    }

    @Override
    public String getId() {
        return "groovy:eval";
    }
}
