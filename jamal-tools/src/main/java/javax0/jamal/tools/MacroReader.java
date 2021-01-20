package javax0.jamal.tools;


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
public class MacroReader {
    final private Processor processor;

    private MacroReader(Processor processor) {
        this.processor = processor;
    }

    public static MacroReader macro(Processor processor) {
        return new MacroReader(processor);
    }

    public Optional<String> readValue(String id) throws BadSyntax {
        final var evaluable = processor.getRegister().getUserDefined(id)
            .filter(macro -> macro instanceof Evaluable)
            .map(macro -> (Evaluable) macro);
        if (evaluable.isPresent()) {
            return Optional.ofNullable(evaluable.get().evaluate());
        } else {
            return Optional.empty();
        }
    }

    public IntegerMacroReader integer() {
        return new IntegerMacroReader();
    }

    public class IntegerMacroReader {
        public Optional<Integer> readValue(String id) throws BadSyntax {
            final var string = MacroReader.macro(MacroReader.this.processor).readValue(id);
            if (string.isPresent()) {
                try {
                    return Optional.of(Integer.parseInt(string.get()));
                } catch (NumberFormatException nfe) {
                    throw new BadSyntax(id + " is not a number");
                }
            } else {
                return Optional.empty();
            }
        }
    }

}