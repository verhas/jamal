package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This macro exports one or more user defined macro to the scope that is one level higher. The names of the user
 * defined macros are comma separated.
 * <p>
 * The returned string that replaces the macro call is empty string.
 */
public class Export implements Macro {

    /**
     * {@inheritDoc}
     *
     * <p> The input is treated as comman delimited list of the user defined names with optional spaces around the
     * commas, for example:
     *
     * <pre>{@code
     *   {@export macro1 ,macro2, macro3 , macro4   }
     * }</pre>
     *
     * The exporting will move the definition of the macro one level higher.
     *
     */
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        var params = Arrays.stream(input.toString()
            .split(","))
            .map(String::trim).collect(Collectors.toSet());
        for (final var param : params) {
            processor.getRegister().export(param);
        }
        return "";
    }
}
