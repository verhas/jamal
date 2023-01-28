package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.MacroReader;

public class Variable extends Expression {
    private final String name;

    public Variable(final String name) {
        this.name = name;
    }

    @Override
    public String execute(final Context ctx) throws BadSyntax {
        ctx.step();
        return MacroReader.macro(ctx.getProcessor()).readValue(name)
                .orElseThrow(() -> new BadSyntax("Variable " + name + " is not defined"));
    }
}
