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
    public String execute(final Processor processor) throws BadSyntax {
        return MacroReader.macro(processor).readValue(name)
                .orElseThrow(() -> new BadSyntax("Variable " + name + " is not defined"));
    }
}
