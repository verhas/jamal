package javax0.jamal.engine.util;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class MacroBodyFetcher {

    /**
     * Eat off a macro from the input caring about all the macro nesting. The input is right after the macro opening
     * string and the optional ident and post evaluate characters and at the end it will eat off from the input not only
     * the macro body but also the last macro closing string characters. The output will be the string that contains the
     * content of the macro including all the matching macro opening and closing strings that are inside if there are
     * any.
     * <p>
     * For example the input (here {@code [[} is the macro opening string and {@code ]]} is the macro closing string):
     *
     * <pre>{@code
     *     \@define z=[[userDef/indef/macro]] some content]]after it is
     *     ^---------------------------------------------------------^
     * }</pre>
     * <p>
     * (see that there is no {@code [[} at the start, as that was already consumed by the processor before calling this
     * method) will return
     *
     * <pre>{@code
     *     \@define z=[[userDef/indef/macro]] some content
     *     ^--------------------------------------------^
     * }</pre>
     * <p>
     * and the input will contain the remaining
     *
     * <pre>{@code
     *     after it is
     *     ^---------^
     * }</pre>
     * <p>
     * (The {@code ^---^} shows where the strings start and end.)
     *
     * @param input the input after the macro opening string
     * @return the output that contains the body of the macro
     * @throws BadSyntaxAt if the macro opening and closing strings are not properly balanced
     */
    public static String getNextMacroBody(final Input input, Processor processor) throws BadSyntaxAt {
        final String output;
        final var macro = Macro.getMacro(processor.getRegister(), input, 0);
        if (macro.isPresent()) {
            output = macro.get().fetch(processor, input);
        } else {
            output = Macro.FETCH.fetch(processor, input);
        }
        return output;
    }
}
