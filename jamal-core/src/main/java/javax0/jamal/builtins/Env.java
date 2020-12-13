package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.api.SpecialCharacters.QUERY;

/**
 * Get the value of an environment variable.
 *
 * <pre>{@code
 * ((@env JAVA_HOME))
 * }</pre>
 * <p>
 * will result the current value of the environment variable {@code JAVA_HOME}.
 * <p>
 * If there is a {@code ?} after the name then the macro returns {@code "true"} (string without the quotes) if the
 * environment variable is defined and {@code "false"} if it is not defined. Testing just the value of the environment
 * variable in an {@code if} macro may be misleading in case the value of the environmen variable is {@code true} or
 * {@code false} or empty string.
 */
public class Env implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var arg = in.toString().trim();
        if (arg.length() == 0) {
            throw new BadSyntax("Empty string as environment variable name");
        }
        final var test = arg.charAt(arg.length() - 1) == QUERY;
        final String name;
        if (test) {
            name = arg.substring(0, arg.length() - 1).trim();
        } else {
            name = arg;
        }
        final var value = System.getenv(name);
        if (test) {
            return "" + (null != value);
        } else {
            return null == value ? "" : value;
        }
    }
}
