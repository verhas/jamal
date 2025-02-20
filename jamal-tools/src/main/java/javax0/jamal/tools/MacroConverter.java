package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Processor;

import java.util.function.Function;

/**
 * Utility class providing methods to convert a macro into a function.
 */
public class MacroConverter {

    /**
     * Converts a user-defined macro into a {@link Function} that takes an array of {@code String} arguments
     * and returns a {@code String} result. The function executes the macro using the provided {@link Processor}.
     *
     * <p>The method retrieves the macro from the processor's register using the specified macro name.
     * If the macro is an instance of {@link Evaluable}, it is converted into a function.
     * Depending on whether the macro is verbatim or not, the function either directly evaluates the macro
     * or processes the result through the processor.</p>
     *
     * @param processor the {@link Processor} instance managing the macro definitions
     * @param macroName the name of the user-defined macro to be converted
     * @return a {@link Function} that evaluates the macro with the provided arguments
     * @throws IllegalArgumentException if the specified macro does not exist in the processor's register
     */
    public static Function<String[], String> toFunction(final Processor processor, final String macroName) {
        return processor.getRegister().getUserDefined(macroName)
                .filter(m -> m instanceof Evaluable)
                .map(m -> (Evaluable) m)
                .map(m -> {
                            if (m.isVerbatim()) {
                                return (Function<String[], String>) args -> {
                                    try {
                                        return m.evaluate(args);
                                    } catch (BadSyntax e) {
                                        throw new RuntimeException(e);
                                    }
                                };
                            } else {
                                return (Function<String[], String>) args -> {
                                    try {
                                        return processor.process(javax0.jamal.tools.Input.makeInput(m.evaluate(args)));
                                    } catch (BadSyntax e) {
                                        throw new RuntimeException(e);
                                    }
                                };
                            }
                        }
                ).orElseThrow(() -> new IllegalArgumentException("Macro " + macroName + " is not defined"));
    }

}
