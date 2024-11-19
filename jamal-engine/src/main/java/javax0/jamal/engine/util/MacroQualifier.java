package javax0.jamal.engine.util;

import javax0.jamal.api.*;
import javax0.jamal.engine.Processor;

import java.util.Set;
import java.util.stream.Collectors;

import static javax0.jamal.tools.InputHandler.*;

public class MacroQualifier {
    public final Input input;
    public String macroId;
    public Evaluable udMacro;
    public final boolean isVerbatim;
    public final boolean isBuiltIn;
    public final Macro macro;
    public int postEvalCount;
    public final Processor processor;
    private static final String NOT_USED = null;


    public MacroQualifier(Processor processor, Input input, int postEvalCount) throws BadSyntax {
        this.processor = processor;
        this.input = input;
        this.postEvalCount = postEvalCount;
        if (macroIsBuiltIn(input)) {
            skip(input, 1);
            skipWhiteSpaces(input);
            macroId = fetchId(input);
            isVerbatim = "verbatim".equals(macroId);
            isBuiltIn = !isVerbatim;
            if (isVerbatim) {
                skipWhiteSpaces(input);
            }
        } else {
            isBuiltIn = false;
            isVerbatim = false;
            macroId = NOT_USED;
        }
        macro = getMacro(macroId, input.getPosition(), isBuiltIn);
    }

    /**
     * Decides if a macro is user defined or build in. The decision is fairly simple because built in macros start with
     * {@code #} or {@code @} character.
     *
     * @param input containing the macro starting at the position zero
     * @return {@code true} if the macro is a built in macro and {@code false} if the macro is user defined
     */
    private boolean macroIsBuiltIn(Input input) {
        return input.length() > 0 && (input.charAt(0) == SpecialCharacters.PRE_EVALUATE || input.charAt(0) == SpecialCharacters.NO_PRE_EVALUATE);
    }

    /**
     * Get the macro object.
     *
     * @param id        the identifier of the macro.
     * @param pos       used to throw exception in case the macro is not defined
     * @param isBuiltIn signals that the macro is built-in. If the macro is not built-in the return value is {@code
     *                  null}.
     * @return the macro object or {@code null} if the macro is not built-in
     * @throws BadSyntaxAt if there is no macro registered for the given name
     */
    private Macro getMacro(String id, Position pos, boolean isBuiltIn) throws BadSyntaxAt {
        if (isBuiltIn) {
            var optMacro = processor.getRegister().getMacro(id);
            if (optMacro.isEmpty()) {
                final Set<String> suggestions = processor.getRegister().suggest(id);
                BadSyntaxAt.when(suggestions.isEmpty(), () -> String.format("There is no built-in macro with the id '%s'", id), pos);
                BadSyntaxAt.when(true,
                        String.format("There is no built-in macro with the id '%s'; did you mean %s?",
                                id, suggestions.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", "))), pos);

            }
            return optMacro.get();
        } else {
            return null;
        }
    }

    public boolean isInnerScopeDependent() {
        return macro instanceof InnerScopeDependent;
    }

}
