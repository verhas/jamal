package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.regex.Pattern;

import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Macros defined in static inner classes that some way help handling regular expressions.
 */
public class Regex {

    /**
     * This macro has the syntax
     * <p>
     * {@code replaceAll   /this is the string where we replace/regular expression/replacement string}
     * <p>
     * The character in the example {@code /} can be any other character. The macro will use the first
     * non-space character as separator. It is an important requirement that the separator character should not
     * appear in the first, second and the third part of the input, otherwise the parsing will not be able to tell
     * where the individual parts start.
     * <p>
     * The result of the macro is replacing all matching occurrence of the regular expression in the string with the
     * third part, the replacement string. This is executed using the Java {@link String#replaceAll(String, String)}
     * method.
     */
    public static class ReplaceAll implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            skipWhiteSpaces(in);
            final var separator = in.substring(0, 1);
            skip(in, 1);
            final var parts = in.toString().split(Pattern.quote(separator), -1);
            if (parts.length != 3) {
                throw new BadSyntax("replaceAll uses " +
                        separator +
                        " as separator but it splits the input into " +
                        parts.length +
                        " parts instead of exactly 3.");
            }
            return parts[0].replaceAll(parts[1],parts[2]);
        }

        @Override
        public String getId() {
            return "replaceAll";
        }
    }

}
