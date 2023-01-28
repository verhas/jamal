package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

public class If implements Command {
    private final Expression condition;
    private final Block then;
    private final Block otherwise;

    public If(final Expression condition, final Block then, final Block otherwise) {
        this.condition = condition;
        this.then = then;
        this.otherwise = otherwise;
    }


    @Override
    public String execute(final Context ctx) throws BadSyntax {
        ctx.step();
        if( Operation.isTrue(condition.execute(ctx))){
            return then.execute(ctx);
        } else {
            return otherwise.execute(ctx);
        }
    }
}
