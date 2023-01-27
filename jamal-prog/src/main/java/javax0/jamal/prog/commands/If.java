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
    public String execute(final Processor processor) throws BadSyntax {
        if( Operation.isTrue(condition.execute(processor))){
            return then.execute(processor);
        } else {
            return otherwise.execute(processor);
        }
    }
}
