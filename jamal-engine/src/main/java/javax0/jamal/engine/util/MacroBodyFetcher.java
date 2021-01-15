package javax0.jamal.engine.util;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.OptionsStore;

import java.util.LinkedList;

import static javax0.jamal.tools.Input.makeInput;
import static javax0.jamal.tools.InputHandler.contains;
import static javax0.jamal.tools.InputHandler.eatEscapedNL;
import static javax0.jamal.tools.InputHandler.move;
import static javax0.jamal.tools.InputHandler.skip;

public class MacroBodyFetcher {

    /**
     * Eat off a macro from the input caring about all the macro nesting. The input is right after the macro opening
     * string and at the end it will eat off from the input not only the macro body but also the last macro closing
     * strings. The output will be the string that contains the content of the macro including all the matching macro
     * opening and closing strings that are inside.
     * <p>
     * For example the input (here {@code [[} is the macro opening string and {@code ]]} is the macro closing string):
     *
     * <pre>{@code
     *     \@define z=[[userDef/indef/macro]] some content]]after it is
     *     ^---------------------------------------------------------^
     * }</pre>
     * <p>
     * (see that there is no {@code [[} at the start) will return
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
        refStack.add(input.getPosition());
        var counter = 1; // we are after one macro opening, so that counts as one opening
        final var output = makeInput();

        while (counter > 0) {// while there is any opened macro
            if (input.length() == 0) {// some macro was not closed
                throw new BadSyntaxAt("Macro was not terminated in the file.", refStack.pop());
            }

            if (input.indexOf(openStr) == 0) {
                move(input, openStr, output);
                refStack.add(input.getPosition());
                counter++; //count the new opening
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
                if (contains(close) && (!contains(open) || close < open)) {
                    limit = close;
                } else {
                    limit = open;
                }
                if (!contains(limit)) {
                    output.append(input);
                    input.reset();
                } else {
                    output.append(input.substring(0, limit));
                    skip(input, limit);
                }
            }
        }
        return output.toString();
    }

}
