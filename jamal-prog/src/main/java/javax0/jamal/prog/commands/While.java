package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

public class While implements Command {
    private final Expression condition;
    private final Block block;

    public While(final Expression condition, final Block block) {
        this.condition = condition;
        this.block = block;
    }


    @Override
    public String execute(final Processor processor) throws BadSyntax {
        final var sb = new StringBuilder();
        while (Operation.isTrue(condition.execute(processor))) {
            sb.append(block.execute(processor));
        }
        return sb.toString();
    }
}
