package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

import java.util.List;

public class Block implements Command{
    private final List<Command> commands;

    public Block(final List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public String execute(final Processor processor) throws BadSyntax {
        final var sb = new StringBuilder();
        for (final var command : commands) {
            sb.append(command.execute(processor));
        }
        return sb.toString();
    }
}
