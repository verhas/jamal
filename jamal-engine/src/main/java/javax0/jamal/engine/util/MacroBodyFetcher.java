package javax0.jamal.engine.util;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Escape;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.api.SpecialCharacters;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.OptionsStore;

import java.util.LinkedList;

import static javax0.jamal.tools.Input.makeInput;
import static javax0.jamal.tools.InputHandler.eatEscapedNL;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.move;
import static javax0.jamal.tools.InputHandler.moveWhiteSpaces;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class MacroBodyFetcher {

    /**
     * Eat off a macro from the input caring about all the macro nesting. The input is right after the macro opening
     * string and at the end it will eat off from the input not only the macro body but also the last macro closing
     * string characters. The output will be the string that contains the content of the macro including all the
     * matching macro opening and closing strings that are inside if there are any.
     * <p>
     * For example the input (here {@code [[} is the macro opening string and {@code ]]} is the macro closing string):
     *
     * <pre>{@code
     *     \@define z=[[userDef/indef/macro]] some content]]after it is
     *     ^---------------------------------------------------------^
     * }</pre>
     * <p>
     * (see that there is no {@code [[} at the start, as that was already consumed by the processor before calling this
     * method) will return
     *
     * <pre>{@code
     *     \@define z=[[userDef/indef/macro]] some content
     *     ^--------------------------------------------^
     * }</pre>
     * <p>
     * and the input will contain the remaining
     *
     * <pre>{@code
     *     after it is
     *     ^---------^
     * }</pre>
     * <p>
     * (The {@code ^---^} shows where the strings start and end.)
     *
     * @param input the input after the macro opening string
     * @return the output that contains the body of the macro
     * @throws BadSyntaxAt if the macro opening and closing strings are not properly balanced
     */
    public static String getNextMacroBody(final Input input, Processor processor) throws BadSyntaxAt {
        final var openStr = processor.getRegister().open();
        final var closeStr = processor.getRegister().close();
        // keep track of all the opened and not yet closed macro start string
        // use it to report error when a macro is not terminated before EOF
        // knowing where the last opening string was that had no closing pair
        var refStack = new LinkedList<Position>();
        refStack.push(input.getPosition());
        var counter = 1; // we are after one macro opening, so that counts as one opening
        final var output = makeInput();

        if (startWithEscapeMacro(processor, input, 0)) {
            moveEscapeMacroBody(input, output, closeStr);
            skip(input, closeStr);
            return output.toString();
        }

        while (counter > 0) {// while there is any opened macro
            if (input.length() == 0) {// some macro was not closed
                throw new BadSyntaxAt("Macro was not terminated in the file.", refStack.pop());
            }

            if (input.indexOf(openStr) == 0) {
                if (startWithEscapeMacro(processor, input, openStr.length())) {
                    move(input, openStr, output);
                    moveEscapeMacroBody(input, output, closeStr);
                    move(input, closeStr, output);
                } else {
                    refStack.push(input.getPosition());
                    move(input, openStr, output);
                    counter++; //count the new opening
                }
            } else if (input.indexOf(closeStr) == 0) {
                counter--; // count the closing
                if (counter == 0) {
                    skip(input, closeStr);
                    if (!OptionsStore.getInstance(processor).is("nl")) {
                        eatEscapedNL(input);
                    }
                } else {
                    refStack.pop();
                    move(input, closeStr, output);
                }
            } else {
                final var open = input.indexOf(openStr);
                final var close = input.indexOf(closeStr);
                final int limit;
                if (close > -1 && (open == -1 || close < open)) {
                    limit = close;
                } else if (open > -1) {
                    limit = open;
                } else {
                    limit = input.length();
                }
                move(input, limit, output);
            }
        }
        return output.toString();
    }

    /**
     * Move the 'escape' macro body to the output.
     * <p>
     * The method is invoked only when {@link #startWithEscapeMacro(Processor, Input, int)} returned  {@code true}. It
     * assumes that the start of the input contains an escape macro.
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
     * @param input    has to point to the start of the macro. After the invocation the input will be stepped onto the
     *                 closing macro string and not after.
     * @param output   where to copy the body of the escape macro. This means all characters between the opening and
     *                 closing strings, including leading and trailing white spaces.
     * @param closeStr the closing string
     * @throws BadSyntaxAt if the syntax of the escape macro is violated. This is not checked by {@link
     *                     #startWithEscapeMacro(Processor, Input, int)}.
     */
    private static void moveEscapeMacroBody(Input input, Input output, String closeStr) throws BadSyntaxAt {
        final var start = input.getPosition();
        moveWhiteSpaces(input, output);
        move(input, 1, output); // the # or @ character
        moveWhiteSpaces(input, output);
        final var escape = fetchId(input);
        output.append(escape);
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
        final var endOfEscapeMacro = input.indexOf(closeStr);
        if (endOfEscapeMacro == -1) {
            throw new BadSyntaxAt("Escape macro is not closed", start);
        }
        move(input, endOfEscapeMacro, output);
    }

    /**
     * Checks that input starts with the 'escape' macro using either # or \@ character. The input is not modified.
     *
     * @param processor the processor
     * @param input     to decide if it start with an escape macro
     * @param offset    is the number of character to skip. This is either zero or the number of cjaracters in the
     *                  opening string.
     * @return true if the input starts with an escape macro
     */
    private static boolean startWithEscapeMacro(Processor processor, Input input, int offset) {
        final var in = makeInput(input);
        skip(in, offset);
        skipWhiteSpaces(in);
        if ((in.length() == 0) ||
            (in.charAt(0) != SpecialCharacters.NO_PRE_EVALUATE && in.charAt(0) != SpecialCharacters.PRE_EVALUATE)) {
            return false;
        }
        skip(in, 1);
        skipWhiteSpaces(in);
        final var macroId = InputHandler.fetchId(in);
        final var macro = processor.getRegister().getMacro(macroId);
        return macro.isPresent() && macro.get() instanceof Escape;
    }


}
