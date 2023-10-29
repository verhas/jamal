package javax0.jamal.api;

/**
 * This interface is extended by the Macro interface to help parameter options parsing.
 * <p>
 * The parameters altering the behaviour of a macro are called parops (parop in singular).
 * This special term is to distinguish it from the parameter or arguments that are passed to the macro.
 * <p>
 * The parops are usually parsed between '(' and ')' characters.
 * Core macros use '[' and ']' characters to parse parops.
 * Some macros use the first line of the input to parse for parops, other the whole input.
 * <p>
 * The prog module can call from BASIC syntax macros and uses the first few arguments as options.
 * It uses '(' and ')' by default to enclose these options before passing them to the macro evaluation.
 * <p>
 * When a macro implements this interface, it can override this defining the {@code optionStart()} and
 * {@code optionEnd()} methods.
 * <p>
 * Note that the return value can be '(' and ')' even when the macro parses the first line or the whole input for
 * options because even in this case, the '(' and ')' can optionally be used.
 */
public interface OptionsControlled {
    default String optionsStart() {
        return "(";
    }

    default String optionsEnd() {
        return ")";
    }

    interface Core extends OptionsControlled {
        @Override
        default String optionsStart() {
            return "[";
        }

        @Override
        default String optionsEnd() {
            return "]";
        }
    }
}
