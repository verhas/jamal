package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.NamedMarker;

/**
 * Start a new evaluation scope. The input of the macro is used in the marker identifying the scope.
 * The same input has to be used in the matching {@link End} macro otherwise the macro evaluation will fail.
 * This helps the user to keep track of the opening and closing scopes.
 *
 * The value of the macro is empty string.
 */
public class Begin implements Macro {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var position = in.getPosition();
        var marker = new NamedMarker(in.toString().trim(), s -> "{@begin " + s + "}", position);
        processor.getRegister().push(marker);
        return "";
    }
}
