package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.api.ScriptMacro;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.getParameters;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Script implements Macro {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntaxAt {
        var ref = input.getPosition();
        skipWhiteSpaces(input);
        var id = fetchId(input);
        skipWhiteSpaces(input);
        final String scriptType;
        if (input.length() > 0 && input.charAt(0) == '/') {
            skip(input, 1);
            scriptType = fetchId(input);
        } else {
            scriptType = "JavaScript";
        }
        skipWhiteSpaces(input);
        final String[] params = getParameters(input, id);
        if (!firstCharIs(input, '=')) {
            throw new BadSyntaxAt("script '" + id + "' has no '=' to body", ref);
        }
        skip(input, 1);
        final ScriptMacro macro;
        try {
            macro = processor.newScriptMacro(id, scriptType, input.toString(), params);
        } catch (BadSyntax bs) {
            throw bs instanceof BadSyntaxAt ?  (BadSyntaxAt)bs : new BadSyntaxAt(bs, ref);
        }
        if (isGlobalMacro(id)) {
            processor.getRegister().global(macro);
        } else {
            processor.getRegister().define(macro);
        }
        return "";
    }
}
