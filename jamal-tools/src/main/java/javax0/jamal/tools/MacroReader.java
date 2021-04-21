package javax0.jamal.tools;


import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Processor;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Utility class to fetch the value of a user defined macro. This is used to get parameter strings, for example the
 * macro {@code NumberLines} has the following lines:
 *
 * <pre>{@code
 *         final var format = UDMacro.macro(processor).readValue("format").orElse("%d. ");
 *         final var start = UDMacro.macro(processor).integer().readValue("start").orElse(1);
 *         final var step = UDMacro.macro(processor).integer().readValue("step").orElse(1);
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
        return _readValue(id);
    }

    private Optional<String> _readValue(String id) throws BadSyntax {
        if( id == null ){
            return Optional.empty();
        }
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
        public OptionalInt readValue(String id) throws BadSyntax {
            final var string = _readValue(id);
            if (string.isPresent()) {
                try {
                    return OptionalInt.of(Integer.parseInt(string.get()));
                } catch (NumberFormatException nfe) {
                    throw new BadSyntax(id + " is not a number");
                }
            } else {
                return OptionalInt.empty();
            }
        }
    }

}
