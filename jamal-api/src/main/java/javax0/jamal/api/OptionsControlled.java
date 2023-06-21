package javax0.jamal.api;

/**
 * This interface is extended by the Macro interface to help parameter parsing.
 * <p>
 * The parameters are usually parsed between '(' and ')' characters.
 * Core macros use '[' and ']' characters to parse parameters.
 * Some macros use the first line of the input to parse for parameters, other the whole input.
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
