package javax0.jamal.tools;


import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Processor;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Utility class to fetch the value of a user-defined macro. This is used to get the value of argument-less macros.
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
        if (id == null) {
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
