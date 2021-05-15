package javax0.jamal.engine.util;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Escape;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.OptionsStore;

import java.util.Optional;

import static javax0.jamal.api.SpecialCharacters.IDENT;
import static javax0.jamal.api.SpecialCharacters.NO_PRE_EVALUATE;
import static javax0.jamal.api.SpecialCharacters.PRE_EVALUATE;
import static javax0.jamal.tools.Input.makeInput;
import static javax0.jamal.tools.InputHandler.eatEscapedNL;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.move;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class MacroBodyFetcher {

    /**
     * Eat off a macro from the input caring about all the macro nesting. The input is right after the macro opening
     * string and the optional ident and post evaluate characters and at the end it will eat off from the input not only
     * the macro body but also the last macro closing string characters. The output will be the string that contains the
     * content of the macro including all the matching macro opening and closing strings that are inside if there are
     * any.
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
        final var output = makeInput();
        final var escapeMacro = getEscapeMacro(processor, input, 0);
        if (escapeMacro.isPresent()) {
            escapeMacro.get().moveBody(processor, input, output);
            skip(input, processor.getRegister().close());
        } else {
            moveNonEscapeBody(processor, input, output);
        }
        return output.toString();
    }

    private static void moveNonEscapeBody(Processor processor, Input input, javax0.jamal.tools.Input output) throws BadSyntaxAt {
        final var open = processor.getRegister().open();
        final var close = processor.getRegister().close();
        var op = new PositionStack(input.getPosition());
        while (op.size() > 0) {// while there is any opened macro
            if (input.length() == 0) {// some macro was not closed
                throw new BadSyntaxAt("Macro was not terminated in the file.", op.pop());
            }

            if (input.indexOf(open) == 0) {
                final int offset = getOffset(input, open);
                final var macro = getEscapeMacro(processor, input, offset);
                if (macro.isPresent()) {
                    move(input, offset, output);
                    macro.get().moveBody(processor, input, output);
                    move(input, close, output);
                } else {
                    op.push(input.getPosition());
                    move(input, open, output);
                }
            } else if (input.indexOf(close) == 0) {
                if (op.popAndEmpty()) {
                    skip(input, close);
                    if (!OptionsStore.getInstance(processor).is("nl")) {
                        eatEscapedNL(input);
                    }
                } else {
                    move(input, close, output);
                }
            } else {
                final var oIndex = input.indexOf(open);
                final var cIndex = input.indexOf(close);
                final int textEnd;
                if (cIndex != -1 && (oIndex == -1 || cIndex < oIndex)) {
                    textEnd = cIndex;
                } else if (oIndex > -1) {
                    textEnd = oIndex;
                } else {
                    textEnd = input.length();
                }
                move(input, textEnd, output);
            }
        }
    }

    private static int getOffset(Input input, String open) {
        int offset = open.length();
        while (offset < input.length() && input.charAt(offset) == IDENT || input.charAt(offset) == PRE_EVALUATE || Character.isWhitespace(input.charAt(offset))) {
            offset++;
        }
        return offset;
    }

    /**
     * Checks that input starts with the 'escape' macro using either # or \@ character. The input is not modified.
     *
     * @param processor the processor
     * @param input     to decide if it start with an escape macro
     * @param offset    is the number of character to skip. This is either zero or the number of characters in the
     *                  opening string.
     * @return the optional escape macro if the macro at the start of the input exists and is an escape macro
     */
    private static Optional<Escape> getEscapeMacro(Processor processor, Input input, int offset) {
        final var in = makeInput(input); // work on a copy of the input
        skip(in, offset);
        skipWhiteSpaces(in);
        if (firstCharIs(in, NO_PRE_EVALUATE, PRE_EVALUATE)) {
            skip(in, 1);
            skipWhiteSpaces(in);
            return processor.getRegister().getMacro(fetchId(in)).filter(m -> m instanceof Escape).map(Escape.class::cast);
        } else {
            return Optional.empty();
        }
    }


}
