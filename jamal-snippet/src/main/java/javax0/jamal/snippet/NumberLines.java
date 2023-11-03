package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

import java.util.IllegalFormatException;

/**
 * Number the lines of the input. For example:
 *
 * <pre>{@code
 *   ((@number
 *   first line
 *   second line
 *   ))
 * }</pre>
 * <p>
 * will return
 *
 * <pre>{@code
 *   1. first line
 *   2. second line
 * }</pre>
 * <p>
 * The formatting can be altered specifying a format string
 *
 * <pre>{@code
 *   ((@define format=%02d. ))
 * }</pre>
 * <p>
 * The format string will be used in {@code String.format()}. The start and step values can also be specified:
 *
 * <pre>{@code
 *   ((@define start=1))
 *   ((@define step=1))
 * }</pre>
 * <p>
 * are the default values.
 */
public class NumberLines implements Macro, InnerScopeDependent, BlockConverter, Scanner.FirstLine {
    @Override
    public String getId() {
        return "numberLines";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var format = scanner.str("format").defaultValue("%d. ");
        final var start = scanner.number("start").defaultValue(1);
        final var step = scanner.number("step").defaultValue(1);
        scanner.done();

        convertTextBlock(processor, in.getSB(), in.getPosition(), format.getParam(), start.getParam(), step.getParam());
        return in.toString();
    }

    public void convertTextBlock(Processor processor, final StringBuilder sb, final Position pos, final Params.Param<?>... params) throws BadSyntax {
        assertParams(3, params);
        final var format = params[0].asString();
        final var start = params[1].asInt();
        final var step = params[2].asInt();

        int i = 0;
        int lineNr = start.get();
        final var fmt = format.get();
        final var stp = step.get();
        while (i > -1) {
            final var formattedNr = getFormattedNr(lineNr, fmt);
            sb.insert(i, formattedNr);
            i += formattedNr.length();
            lineNr += stp;
            i = sb.indexOf("\n", i);
            if (i != -1) {
                i++;
            }
            if (i >= sb.length()) {
                break;
            }
        }
    }

    private String getFormattedNr(int lineNr, String fmt) throws BadSyntax {
        final String formattedNr;
        try {
            formattedNr = String.format(fmt, lineNr);
        } catch (IllegalFormatException e) {
            throw new BadSyntax("The format string in macro '" + getId() + "' is incorrect.", e);
        }
        return formattedNr;
    }
}
