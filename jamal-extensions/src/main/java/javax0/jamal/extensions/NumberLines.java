package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

/**
 * Number the lines of the input. For example:
 *
 * <pre>{@code
 *   ((@numberLines
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
public class NumberLines implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var format = UDMacro.macro("format").from(processor).orElse("%d. ");
        final var start = UDMacro.macro("start").integer().from(processor).orElse(1);
        final var step = UDMacro.macro("step").integer().from(processor).orElse(1);
        InputHandler.skipWhiteSpaces2EOL(in);
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

    private int getInt(Processor processor, String s) throws BadSyntax {
        int start;
        try {
            start = Integer.parseInt(UDMacro.macro(s).from(processor).orElse("1"));
        } catch (NumberFormatException nfe) {
            throw new BadSyntax(s + " is not a number");
        }
        return start;
    }
}
