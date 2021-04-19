package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

/**
 * W A R N I N G!!!!
 * <p>
 * Before changing anything here, read the following and make sure you understand the consequences!
 * <p>
 * Jamal source text can use this macro to escape macro opening and closing strings. The syntax of the macro is
 *
 * <pre>{@code
 *         {@escape `x` escaped text presumably containing
 *                        macro opening and/or closing strings
 *                        possibly multiple times `x`}
 * }</pre>
 * <p>
 * Where {@code `x`} is an arbitrary string between backtick characters. It stands at the start of the macro after the
 * keyword and also signals the end of the escaped string.
 * <p>
 * The implementation of this macro heavily depends on the implementation of the class {@code
 * javax0.jamal.engine.util.MacroBodyFetcher}. When fetching the content of a new macro, the macro body fetcher looks
 * ahead and counting the opening and closing macro strings finds the matching closing string for the macro it fetches.
 * For example, it fetches a macro at a point where the input is
 *
 * <pre>{@code
 *           {@define a=2{b}}
 * }</pre>
 * <p>
 * it will fetch "{@code @define a=2{b}}" as it is counting the { and the } characters. If there is an {@code escape}
 * macro then the counting should be paused between the {@code `x`} parts. For example
 *
 * <pre>{@code
 *           {@escape `st` } `st`}
 * }</pre>
 * <p>
 * will fetch "{@code @escape `st` }} {@code `st`}" because it skips the counting in the part between the {@code `st`}.
 *
 * This way, this macro is "implemented" not only here but also in the macro body fetcher.
 */
public class Escape implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        if (in.charAt(0) != '`') {
            throw new BadSyntaxAt("The macro escape needs an escape string enclosed between ` characters.", in.getPosition());
        }
        InputHandler.skip(in, 1);
        final var endOfEscape = in.indexOf("`");
        if (endOfEscape == -1) {
            throw new BadSyntaxAt("The macro escape needs an escape string enclosed between ` characters. Closing ` is not found.", in.getPosition());
        }
        final var escapeSequence = "`" + in.subSequence(0, endOfEscape).toString() + "`";
        InputHandler.skip(in, escapeSequence.length() - 1);
        final var endOfString = in.indexOf(escapeSequence);
        if (endOfString == -1) {
            throw new BadSyntaxAt("I cannot find the escape string at the end of the macro: " + escapeSequence, in.getPosition());
        }
        final var escapedString = in.substring(0, endOfString);
        InputHandler.skip(in, escapedString);
        InputHandler.skip(in, escapeSequence);
        InputHandler.skipWhiteSpaces(in);
        if (in.length() > 0) {
            throw new BadSyntaxAt("There are extra characters in the use of {@escape } after the closing escape sequence: " + escapeSequence, in.getPosition());
        }
        return escapedString;
    }
}
