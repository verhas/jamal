package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Configurable;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.api.SpecialCharacters.DEFINE_OPTIONALLY;
import static javax0.jamal.api.SpecialCharacters.DEFINE_VERBATIM;
import static javax0.jamal.api.SpecialCharacters.ERROR_REDEFINE;
import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.getParameters;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Define implements Macro {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);
        boolean verbatim = false;
        if (InputHandler.firstCharIs(input, DEFINE_VERBATIM)) {
            verbatim = true;
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        var optional = InputHandler.firstCharIs(input, DEFINE_OPTIONALLY);
        var noRedefine = InputHandler.firstCharIs(input, ERROR_REDEFINE);
        if (optional || noRedefine) {
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        if (!verbatim && InputHandler.firstCharIs(input, DEFINE_VERBATIM)) {
            verbatim = true;
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        var id = fetchId(input);
        if (processor.isDefined(convertGlobal(id))) {
            if (optional) {
                return "";
            }
            if (noRedefine) {
                throw new BadSyntax("The macro '" + id + "' was already defined.");
            }
        }
        skipWhiteSpaces(input);
        final boolean pureByBeforeParams = id.endsWith(":") && !firstCharIs(input, '(');
        if (pureByBeforeParams) {
            id = id.substring(0, id.length() - 1);
        }
        final String[] params = getParameters(input, id);
        final boolean pure = pureByBeforeParams || firstCharIs(input, ':');
        if (firstCharIs(input, ':')) {
            skip(input, 1);
        }
        if (!firstCharIs(input, '=')) {
            throw new BadSyntax("define '" + id + "' has no '=' to body");
        }
        skip(input, 1);
        final var macro = processor.newUserDefinedMacro(convertGlobal(id), input.toString(), verbatim, params);
        if (isGlobalMacro(id)) {
            processor.defineGlobal(macro);
        } else {
            processor.define(macro);
        }
        if (pure && macro instanceof Configurable) {
            ((Configurable) macro).configure("pure", true);
        }
        return "";
    }
}
