package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.Arrays;
import java.util.Objects;

/**
 * Block converter macros implement this interface.
 */
public interface BlockConverter {
    /**
     * This method is usually called from inside from the method
     * {@link javax0.jamal.api.Macro#evaluate(Input, Processor) evaluate()}, but it is also possible to invoked it
     * from other macros. It separates the input parsing for parameters and the actual processing of the block.
     *
     * @param sb     the input and also the store for the output
     * @param pos    the position where the input starts
     * @param params the parameters for the processing. It is different for every block converter macro.
     * @throws BadSyntax when the underlying macro throws up
     */
    void convertTextBlock(StringBuilder sb, Position pos, Params.Param<?>... params) throws BadSyntax;

    /**
     * Use this method in the macros implementing this interface to check that the caller was passing the right
     * amount of parameters.
     */
    default void assertParams(int numberOfParams, Params.Param<?>[] params) {
        if (params.length != numberOfParams || Arrays.stream(params).anyMatch(Objects::isNull)) {
            final var macro = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass().getSimpleName();
            throw new IllegalArgumentException("The number of parameters is "
                    + params.length
                    + " but it should be "
                    + numberOfParams
                    + " in the macro class "
                    + macro
                    + ". This is an internal error.");
        }
    }
}
