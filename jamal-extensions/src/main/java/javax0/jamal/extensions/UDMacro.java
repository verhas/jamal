package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Processor;

import java.util.Optional;

/**
 * Utility class to fetch the value of a user defined macro. This is used to get parameter strings, for example the
 * macro {@code NumberLines} has the following lines:
 *
 * <pre>{@code
 *         final var format = UDMacro.macro("format").from(processor).orElse("%d. ");
 *         final var start = UDMacro.macro("start").integer().from(processor).orElse(1);
 *         final var step = UDMacro.macro("step").integer().from(processor).orElse(1);
 * }</pre>
 * <p>
 * to fetch the string value of the macros {@code format}, {@code start}, {@code step}. When the method {@code
 * integer()} is inserted into the call chain then the returned value is converted to integer.
 */
public class UDMacro {
    final private String id;

    private UDMacro(String id) {
        this.id = id;
    }

    public static UDMacro macro(String id) {
        return new UDMacro(id);
    }

    public Optional<String> from(Processor processor) throws BadSyntax {
        final var evaluable = processor.getRegister().getUserDefined(id)
            .filter(macro -> macro instanceof Evaluable)
            .map(macro -> (Evaluable) macro);
        if (evaluable.isPresent()) {
            return Optional.ofNullable(evaluable.get().evaluate());
        } else {
            return Optional.empty();
        }
    }

    public IntUDMac integer() {
        return new IntUDMac();
    }

    public class IntUDMac {
        public Optional<Integer> from(Processor processor) throws BadSyntax {
            final var string = UDMacro.this.from(processor);
            if (string.isPresent()) {
                try {
                    return Optional.of(Integer.parseInt(string.get()));
                } catch (NumberFormatException nfe) {
                    throw new BadSyntax(UDMacro.this.id + " is not a number");
                }
            } else {
                return Optional.empty();
            }
        }
    }

}
