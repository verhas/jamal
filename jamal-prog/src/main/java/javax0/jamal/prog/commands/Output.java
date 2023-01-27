package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

public class Output implements Command  {
    private final Expression expression;

    public Output(final Expression expression) {
        this.expression = expression;
    }

    @Override
    public String execute(final Processor processor) throws BadSyntax {
        return expression.execute(processor);
    }
}
