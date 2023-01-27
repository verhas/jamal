package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

import java.util.Objects;

public class Constant extends Expression {
    private final String value;

    public Constant(final String value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public String execute(final Processor processor) throws BadSyntax {
        return value;
    }
}
