package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.api.SpecialCharacters.DEFINE_OPTIONALLY;
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
        var optional = InputHandler.firstCharIs(input, DEFINE_OPTIONALLY);
        if (optional) {
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        var id = fetchId(input);
        if (optional && processor.isDefined(id)) {
            return "";
        }
        skipWhiteSpaces(input);
        final String[] params = getParameters(input, id);
        if (!firstCharIs(input, '=')) {
            throw new BadSyntax("define '" + id + "' has no '=' to body");
        }
        skip(input, 1);
        if (isGlobalMacro(id)) {
            var macro = processor.newUserDefinedMacro(convertGlobal(id), input.toString(), params);
            processor.defineGlobal(macro);
        } else {
            var macro = processor.newUserDefinedMacro(id, input.toString(), params);
            processor.define(macro);
        }
        return "";
    }
}
