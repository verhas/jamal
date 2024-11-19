package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import static javax0.jamal.tools.InputHandler.*;
import static javax0.jamal.tools.ScriptingTools.getEngine;
import static javax0.jamal.tools.ScriptingTools.resultToString;

public class Eval implements Macro, InnerScopeDependent, OptionsControlled.Core, Scanner.Core {
    //snippet DEFAULT_LOOP_LIMIT
    private static final int DEFAULT_LOOP_LIMIT = 100;
    // end snippet

    /**
     * {@inheritDoc}
     */
    @Override
    public String evaluate(final Input input, final Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);
        final String scriptType = getScriptType(input);

        processor.getRegister().lock();
        switch (scriptType) {
            case "*":
                final var scanner = newScanner(input, processor);
                // snippet evaluateLoopLimit
                final var limit = scanner.number("evaluateLoopLimit", "limit", "max").defaultValue(DEFAULT_LOOP_LIMIT);
                // end snippet
                scanner.done();
                int loopCounter = limit.get();
                String result;
                final Position pos = input.getPosition();
                Input loopInput = input;
                while (true) {
                    var penultimate = loopInput.toString();
                    result = processor.process(loopInput);
                    if (penultimate.equals(result)) {
                        break;
                    }
                    loopInput = javax0.jamal.tools.Input.makeInput(result, pos);
                    loopCounter--;
                    BadSyntax.when(loopCounter == 0, "eval* probably got into an infinite loop");
                }
                return result;
            case "jamal":
                return processor.process(input);
            case "JShell":
                final var shell = processor.getJShellEngine();
                BadSyntax.when(shell == null, "JShell engine is not available");
                return shell.evaluate(input.toString());
            default:
                var engine = getEngine(scriptType);
                try {
                    return resultToString(engine.eval(input.toString()));
                } catch (Exception e) {
                    throw new BadSyntax("Script in eval threw exception", e);
                }
        }
    }

    /**
     * Get the script type. The script type is given after the macro name, {@code eval} following a {@code /}
     * character. There can be space before the {@code /} character.
     * <p>
     * If the first non-whitespace character following the name of the macro is not a {@code /} then the script type is
     * {@code jamal}. If the script type has to be {@code jamal} but the script itself starts with a {@code /}
     * character then the use of the macro should explicitly define
     * <pre>{@code eval/jamal   /script starting with slash}</pre>
     * the script type as {@code jamal}
     *
     * @param input that contains the type of the script
     * @return the identifier of the script type
     */
    private static String getScriptType(Input input) {
        final String scriptType;
        if (input.length() > 0 && input.charAt(0) == '/') {
            skip(input, 1);
            skipWhiteSpaces(input);
            scriptType = fetchId(input);
            skipWhiteSpaces(input);
        } else if (input.length() > 0 && input.charAt(0) == '*') {
            skip(input, 1);
            scriptType = "*";
            skipWhiteSpaces(input);
        } else {
            scriptType = "jamal";
        }
        return scriptType;
    }
}
/*template jm_eval
{template |eval|eval/$S$ $C$|evaluate the content as script|
  {variable |S|"script"}
  {variable |C|"..."}
}
 */