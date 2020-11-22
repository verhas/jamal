package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;

public class SnippetMacros {

    /**
     * Take the argument of the macro and
     * removes N spaces from the start of each line so that there is at least one line that does not start with a space
     * character.
     * <p>
     * This can be used, when a snippet is included into the macro file and some program code is tabulated. In that case
     * this snippet will be moves to the left as much as possible.
     */
    public static class Trim implements Macro {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            skipWhiteSpaces2EOL(in);
            final var sb = in.getSB();
            int minSpaces = Integer.MAX_VALUE;
            for (int i = 0; i < sb.length(); ) {
                int spaceCount = 0;
                while (i < sb.length() && Character.isWhitespace(sb.charAt(i)) && sb.charAt(1) != '\n') {
                    i++;
                    spaceCount++;
                }
                minSpaces = Math.min(minSpaces, spaceCount);
                int index = sb.indexOf("\n", i);
                if (index == -1) break;
                i = index + 1;
            }
            for (int i = 0; i < sb.length(); ) {
                sb.delete(i, i + minSpaces);
                int index = sb.indexOf("\n", i);
                if (index == -1) break;
                i = index + 1;
            }
            return sb.toString();
        }
    }

    /**
     * Number the lines of the input. For example:
     *
     * <pre>{@code
     *   ((@number
     *   first line
     *   second line
     *   ))
     * }</pre>
     *
     * will return
     *
     * <pre>{@code
     *   1. first line
     *   2. second line
     * }</pre>
     *
     * The formatting can be altered specifying a format string
     *
     * <pre>{@code
     *   ((@define format=%02d. ))
     * }</pre>
     *
     * The format string will be used in {@code String.format()}.
     * The start and step values can also be specified:
     *
     * <pre>{@code
     *   ((@define start=1))
     *   ((@define step=1))
     * }</pre>
     *
     * are the default values.
     *
     */
    public static class Number implements Macro, InnerScopeDependent {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var format = UDMacro.macro("format").from(processor).orElse("%d. ");
            final var start = UDMacro.macro("start").integer().from(processor).orElse(1);
            final var step = UDMacro.macro("step").integer().from(processor).orElse(1);
            skipWhiteSpaces2EOL(in);
            int i = 0;
            final var sb = in.getSB();
            int lineNr = start;
            while (i > -1) {
                final var formattedNr = String.format(format, lineNr);
                sb.insert(i, formattedNr);
                i += formattedNr.length();
                lineNr += step;
                i = sb.indexOf("\n", i);
                if (i != -1) {
                    i++;
                }
                if (i >= sb.length()) {
                    break;
                }
            }
            return in.toString();
        }
    }
}
