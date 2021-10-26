package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.OptionsStore;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.move;
import static javax0.jamal.tools.InputHandler.moveWhiteSpaces;
import static javax0.jamal.tools.InputHandler.skip;

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
 * <p>
 * This way, this macro is "implemented" not only here but also in the macro body fetcher.
 */
public class Escape implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final boolean fullPreserve;
        if (in.charAt(0) == '*') {
            InputHandler.skip(in, 1);
            fullPreserve = ! OptionsStore.getInstance(processor).is(Unescape.UNESCAPE_OPTION);
        } else {
            fullPreserve = false;
        }
        InputHandler.skipWhiteSpaces(in);
        if (in.charAt(0) != '`') {
            throw new BadSyntaxAt("The macro escape needs an escape string enclosed between ` characters.", in.getPosition());
        }
        InputHandler.skip(in, 1);
        final var endOfEscape = in.indexOf("`");
        if (endOfEscape == -1) {
            throw new BadSyntaxAt("The macro escape needs an escape string enclosed between ` characters. Closing ` is not found.", in.getPosition());
        }
        final var escapeSequence = "`" + in.subSequence(0, endOfEscape) + "`";
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
        if (fullPreserve) {
            return processor.getRegister().open() + "@escape*" + escapeSequence + escapedString + escapeSequence + processor.getRegister().close();
        }
        return escapedString;
    }


    /**
     * Move the 'escape' macro body to the output.
     * <p>
     * The method is invoked only when {@code getEscapeMacro(Processor, Input, int)} returned  {@code true}. It assumes
     * that the start of the input contains an escape macro.
     * <p>
     * Note that this method will not care if there are extra non-whitespace character between the second {@code `xxx`}
     * string and the macro closing string, like in
     *
     * <pre>{@code
     *     {@escape `|||`escaped content `|||` this is erroneous}
     * }</pre>
     * <p>
     * the characters "{@code this is erroneous}". This will however be an error when the macro as implemented in the
     * module {@code jamal.core} is executed.
     *
     * @param processor the Jamal processor
     * @param input     has to point to the start of the macro. After the invocation the input will be stepped onto the
     *                  closing macro string and not after.
     * @return the body of the macro
     * @throws BadSyntaxAt if the syntax of the escape macro is violated.
     */
    @Override
    public String fetch(Processor processor, Input input) throws BadSyntaxAt {
        final var output = javax0.jamal.tools.Input.makeInput();
        final var start = input.getPosition();
        moveWhiteSpaces(input, output);
        move(input, 1, output); // the # or @ character
        moveWhiteSpaces(input, output);
        output.append(fetchId(input));
        if( input.charAt(0) == '*'){
            output.append('*');
            skip(input,1);
        }
        moveWhiteSpaces(input, output);
        if (input.charAt(0) != '`') {
            throw new BadSyntaxAt("The macro escape needs an escape string enclosed between ` characters.", input.getPosition());
        }
        move(input, 1, output);
        final var endOfEscape = input.indexOf("`");
        if (endOfEscape == -1) {
            throw new BadSyntaxAt("The macro escape needs an escape string enclosed between ` characters. Closing ` is not found.", input.getPosition());
        }
        final var escapeSequence = "`" + input.subSequence(0, endOfEscape).toString() + "`";
        move(input, escapeSequence.length() - 1, output);
        final var endOfString = input.indexOf(escapeSequence);
        if (endOfString == -1) {
            throw new BadSyntaxAt("I cannot find the escape string at the end of the macro: " + escapeSequence, input.getPosition());
        }
        move(input, endOfString, output);
        move(input, escapeSequence.length(), output);
        final var closeStr = processor.getRegister().close();
        final var endOfEscapeMacro = input.indexOf(closeStr);
        if (endOfEscapeMacro == -1) {
            throw new BadSyntaxAt("Escape macro is not closed", start);
        }
        move(input, endOfEscapeMacro, output);
        skip(input, closeStr);
        return output.toString();
    }

}
