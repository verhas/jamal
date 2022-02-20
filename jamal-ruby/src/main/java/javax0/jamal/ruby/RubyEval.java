package javax0.jamal.ruby;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class RubyEval implements Macro, InnerScopeDependent {
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        try {
            final var shell = Shell.getShell(in, processor, this);
            return "" + shell.evaluate(in.toString(), null);
        } catch (Exception e) {
            throw new BadSyntax("Error evaluating ruby script using eval", e);
        }
    }

    @Override
    public String getId() {
        return "ruby:eval";
    }
}
