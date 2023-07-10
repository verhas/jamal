package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;

import java.util.List;

public class Block implements Command{
    private final List<Command> commands;

    public Block(final List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public String execute(final Context ctx) throws BadSyntax {
        ctx.step();
        final var sb = new StringBuilder();
        for (final var command : commands) {
            sb.append(command.execute(ctx));
        }
        return sb.toString();
    }
}
