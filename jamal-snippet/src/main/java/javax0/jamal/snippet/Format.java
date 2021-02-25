package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Cast;
import javax0.jamal.tools.InputHandler;

import java.util.IllegalFormatException;
import java.util.Optional;
import java.util.function.Function;

public class Format implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final String[] parts = InputHandler.getParts(in);
        final String format = parts[0];
        try {
            final Object[] value = new Object[parts.length - 1];
            for (int i = 0; i < value.length; i++) {
                value[i] = Cast.cast(parts[i + 1]);
            }
            return String.format(format, value);
        } catch (IllegalFormatException e) {
            throw new BadSyntax("The format string '" + format + "'in macro '" + getId() + "' is incorrect.", e);
        }
    }
}
