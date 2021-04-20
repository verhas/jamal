package javax0.jamal.engine.util;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.SpecialCharacters;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.OptionsStore;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class MacroQualifier {
    public final Input input;
    public String macroId;
    public Evaluable udMacro;
    public final boolean isVerbatim;
    public final boolean isBuiltIn;
    public final Macro macro;
    public final boolean oldStyle;
    public int postEvalCount;
    public final Processor processor;
    private final String NOT_USED = null;


    public MacroQualifier(Processor processor, Input input, int postEvalCount) throws BadSyntax {
        this.processor = processor;
        this.input = input;
        this.postEvalCount = postEvalCount;
        this.oldStyle = OptionsStore.getInstance(processor).is("omasalgotm");
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
     * @param macroId   the identifier of the macro.
     * @param pos       used to throw exception in case the macro is not defined
     * @param isBuiltIn signals that the macro is built-in. If the macro is not built-in the return value is {@code
     *                  null}.
     * @return the macro object or {@code null} if the macro is not built-in
     * @throws BadSyntaxAt if there is no macro registered for the given name
     */
    private Macro getMacro(String macroId, Position pos, boolean isBuiltIn) throws BadSyntaxAt {
        if (isBuiltIn) {
            var optMacro = processor.getRegister().getMacro(macroId);
            if (optMacro.isEmpty()) {
                throw new BadSyntaxAt(
                    "There is no built-in macro with the id '"
                        + macroId
                        + "'", pos);
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
