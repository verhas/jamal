package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class Assignment implements Command {

    private final String variable;
    private final Expression expression;

    public Assignment(final String variable, final Expression expression) {
        this.variable = variable;
        this.expression = expression;
    }

    public static void let(final Processor processor, final String variable, final String value) throws BadSyntax {
        final var macro = processor.newUserDefinedMacro(InputHandler.convertGlobal(variable), value);

        if (InputHandler.isGlobalMacro(variable)) {
            processor.defineGlobal(macro);
        } else {
            processor.define(macro);
        }
    }

    @Override
    public String execute(final Context ctx) throws BadSyntax {
        ctx.step();
        final var processor = ctx.getProcessor();
        if (variable == null) {
            expression.execute(ctx);
        } else {
            let(processor, variable, expression.execute(ctx));
        }
        return "";
    }
}
