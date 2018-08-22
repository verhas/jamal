package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.*;

public class Define implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        var input = in.getInput();
        skipWhiteSpaces(input);
        var optional = input.charAt(0) == '?';
        if (optional) {
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        var id = fetchId(input);
        if (optional && macroIsAlreadyDefined(processor, id)) {
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
            processor.getRegister().global(macro);
        } else {
            var macro = processor.newUserDefinedMacro(id, input.toString(), params);
            processor.getRegister().define(macro);
        }
        return "";
    }

    private boolean macroIsAlreadyDefined(Processor processor, String id) {
        return processor.getRegister().getUserMacro(id).isPresent();
    }
}
