package javax0.jamal.engine.util;

import javax0.jamal.api.Input;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.api.SpecialCharacters.IDENT;
import static javax0.jamal.api.SpecialCharacters.POST_VALUATE;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Compose a macro prefix object reading the input. The macro prefix is the string between the macro open string and the
 * macro starting {@code #} or  {@code @} character.
 */
public class PrefixComposer {

    /**
     * A {@code Prefix} object contains,
     *
     * <ul>
     *     <li>{@code postEvalCount} is the number of {@code !} characters in the macro prefix
     *     <li>{@code identCount} is the number of back-tick characters in the macro prefix
     *     <li>{@code string} is the prefix that was read with the first back-tick character removed, if there was any
     * </ul>
     */
    public static class Prefix {
        public final int postEvalCount;
        public final int identCount;
        public final String string;

        private Prefix(int postEvalCount, int identCount, String string) {
            this.postEvalCount = postEvalCount;
            this.identCount = identCount;
            this.string = string;
        }
    }

    /**
     * Read the input removing the prefix from the start and returning the created {@link Prefix} object.
     *
     * @param input the input following the macro opening string. If there are any spaces before the prefix they will be
     *              skipped.
     * @return the new {@link Prefix} object
     */
    public static Prefix compose(Input input) {
        int postEvalCount = 0;
        int identCount = 0;
        skipWhiteSpaces(input);
        final var prefix = new StringBuilder();
        while (firstCharIs(input, POST_VALUATE, IDENT)) {
            if (firstCharIs(input, POST_VALUATE)) {
                postEvalCount++;
                InputHandler.move(input, 1, prefix);
            } else {
                identCount++;
                if (identCount > 1) {
                    InputHandler.move(input, 1, prefix);
                } else {
                    skip(input, 1);
                }
            }
            InputHandler.moveWhiteSpaces(input, prefix);
        }
        return new Prefix(postEvalCount, identCount, prefix.toString());
    }
}
