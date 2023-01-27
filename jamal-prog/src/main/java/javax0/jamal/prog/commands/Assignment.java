package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

public class Assignment implements Command {

    private final String variable;
    private final Expression expression;

    public Assignment(final String variable, final Expression expression) {
        this.variable = variable;
        this.expression = expression;
    }

    public static void let(final Processor processor, final String variable, final String value) throws BadSyntax {
        processor.define(processor.newUserDefinedMacro(variable, value));
    }

    @Override
    public String execute(final Processor processor) throws BadSyntax {
        let(processor,variable, expression.execute(processor));
        return "";
    }
}
