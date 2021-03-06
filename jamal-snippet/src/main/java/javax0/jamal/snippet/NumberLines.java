package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.IllegalFormatException;

import static javax0.jamal.tools.Params.holder;

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
public class NumberLines implements Macro, InnerScopeDependent {
    @Override
    public String getId() {
        return "numberLines";
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var format = holder("format").orElse("%d. ").asString();
        final var start = holder("start").orElseInt(1);
        final var step = holder("step").orElseInt(1);
        Params.using(processor).from(this).keys(format, start, step).parse(in);

        int i = 0;
        final var sb = in.getSB();
        int lineNr = start.get();
        while (i > -1) {
            final String formattedNr;
            try {
                formattedNr = String.format(format.get(), lineNr);
            } catch (IllegalFormatException e) {
                throw new BadSyntax("The format string in macro '" + getId() + "' is incorrect.", e);
            }
            sb.insert(i, formattedNr);
            i += formattedNr.length();
            lineNr += step.get();
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
