package javax0.jamal.builtins;

import javax0.jamal.api.*;

import static javax0.jamal.tools.InputHandler.*;

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
        final UserDefinedMacro macro;
        try {
            macro = processor.newUserDefinedMacro(id, input.toString(), params);
        } catch (BadSyntax bs) {
            throw new BadSyntaxAt(bs, ref);
        }
        macro.setScriptType(scriptType);
        if (isGlobalMacro(id)) {
            processor.getRegister().global(macro);
        } else {
            processor.getRegister().define(macro);
        }
        return "";
    }
}
