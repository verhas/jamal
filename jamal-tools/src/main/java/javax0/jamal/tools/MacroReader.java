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

    /**
     * Create a new instance of the macro reader.
     *
     * @param processor the processor that is used to get the value of the macro
     * @return the new instance
     */
    public static MacroReader macro(Processor processor) {
        return new MacroReader(processor);
    }

    /**
     * Read the value of the macro calling its evaluate method.
     * The macro must work without any argument.
     *
     * @param id the name of the macro
     * @return the value of the macro evaluated optionally. It is an empty optional if the macro is not defined.
     * @throws BadSyntax if the macro is defined but the evaluation throws an exception
     */
    public Optional<String> readValue(String id) throws BadSyntax {
        return read(processor, id);
    }

    /**
     * Transforms the macro reader to a new macro reader that reads integer values.
     *
     * @return the new macro reader
     */
    public IntegerMacroReader integer() {
        return new IntegerMacroReader();
    }

    public class IntegerMacroReader {

        /**
         * Read the value of the macro calling its evaluate method and return it as an integer.
         *
         * @param id the name of the macro
         * @return the value of the macro evaluated optionally. It is an empty optional if the macro is not defined.
         * @throws BadSyntax if the macro is defined but the evaluation throws an exception or the value is not an integer
         */
        public OptionalInt readValue(String id) throws BadSyntax {
            final var string = read(processor, id);
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

    /**
     * private helper method reading and evaluating the macro.
     *
     * @param processor the processor to use to get the macro
     * @param id the name of the macro
     * @return the value of the macro evaluated optionally. It is an empty optional if the macro is not defined.
     * @throws BadSyntax if the macro is defined but the evaluation throws an exception
     */
    private static Optional<String> read(Processor processor, String id) throws BadSyntax {
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
}
