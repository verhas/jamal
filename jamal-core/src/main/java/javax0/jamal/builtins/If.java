package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Define the {@code if} conditional macro. The syntax of the macro is
 *
 * <pre>
 *     {#if/test/then content/else content}
 * </pre>
 * <p>
 * The result of the evaluated macro will be the {@code then content} when the {@code test} is true and the {@code else}
 * content otherwise. The {@code test} is true, if it is the literal {@code "true"} (case insensitive), an integer
 * number and the value is not zero or any other string that contains at least one non-space character, except when the
 * {@code test} is the literal {@code "false"} (case insensitive) then the test is false.
 * <p>
 * The syntax depicted above using the {@code /} character as separator. It is only convention. Any non-space character
 * can be used as separator. The first non-space character following the {@code if} will be used as separator
 * character.
 */
public class If implements Macro {

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var pos = input.getPosition();
        final var parts = InputHandler.getParts(input,3);
        if( parts.length < 1 ){
            throw new BadSyntaxAt("Macro 'if' needs 1, 2 or 3 arguments",pos);
        }
        if (isTrue(parts[0])) {
            return parts[1];
        } else {
            return parts.length > 2 ? parts[2] : "";
        }
    }

    private boolean isTrue(String test) {
        if (test.trim().equalsIgnoreCase("true")) {
            return true;
        }
        if (test.trim().equalsIgnoreCase("false")) {
            return false;
        }
        if (test.trim().matches("[+-]?\\d+")) {
            return Integer.parseInt(test) != 0;
        }
        return test.trim().length() > 0;
    }

}
