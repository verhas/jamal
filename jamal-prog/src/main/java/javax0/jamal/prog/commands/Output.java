package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;

public class Output implements Command {
    private final Expression expression;

    public Output(final Expression expression) {
        this.expression = expression;
    }

    @Override
    public String execute(final Context ctx) throws BadSyntax {
        ctx.step();
        return expression.execute(ctx);
    }
}
