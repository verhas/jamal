package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.*;
import static javax0.jamal.tools.ScriptingTools.getEngine;
import static javax0.jamal.tools.ScriptingTools.resultToString;

public class Eval implements Macro {
    @Override
    public String evaluate(final Input input, final Processor processor) throws BadSyntax {
        final String scriptType;
        skipWhiteSpaces(input);
        if (input.length() > 0 && input.charAt(0) == '/') {
            skip(input, 1);
            scriptType = fetchId(input);
            skipWhiteSpaces(input);
        } else {
            return processor.process(input);
        }
        if (scriptType.equals("jamal")) {
            return processor.process(input);
        }
        var engine = getEngine(scriptType);
        try {
            return resultToString(engine.eval(input.toString()));
        } catch (Exception e) {
            throw new BadSyntax("Script in eval threw exception", e);
        }
    }
}