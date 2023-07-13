package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.api.SpecialCharacters.QUERY;
import static javax0.jamal.api.SpecialCharacters.REPORT_ERRMES;

/**
 * Get the value of an environment variable. For example,
 *
 * <pre>{@code
 * {@env JAVA_HOME}
 * }</pre>
 * <p>
 * will result the current value of the environment variable {@code JAVA_HOME}.
 * <p>
 * If there is a {@code ?} after the name then the macro returns "{@code true}" (string without the quotes) if the
 * environment variable is defined and "{@code false}" if it is not defined. Testing just the value of the environment
 * variable in an {@code if} macro may be misleading in case the value of the environment variable is {@code true} or
 * {@code false} or empty string.
 * <p>
 * If there is a {@code !} after the name then the macro will throw an exception in case the environment variable is
 * not defined.
 * <p>
 * The input of the macro is trimmed at the start, so starting and ending spaces do not count. If the last character
 * after the trimming is {@code ?} or  {@code !} it is chopped off and the string is chopped again. It means that you
 * can have spaces between the name and the {@code ?} or  {@code !} character.
 * <p>
 * You cannot have both {@code ?} and  {@code !} characters. If your environment variable name ends with the {@code ?}
 * or  {@code !} character then you cannot use this macro, and you are in trouble. However, in that case you are a sick
 * bastard, and you are in trouble anyway.
 */
public class Env implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var arg = in.toString().trim();
        BadSyntax.when(arg.length() == 0, "Empty string as environment variable name");
        final var test = arg.charAt(arg.length() - 1) == QUERY;
        final var report = arg.charAt(arg.length() - 1) == REPORT_ERRMES;
        final String name;
        if (test || report) {
            name = arg.substring(0, arg.length() - 1).trim();
        } else {
            name = arg;
        }
        final var value = EnvironmentVariables.getenv(name).orElse(null);
        if (test) {
            return "" + (null != value);
        } else {
            BadSyntax.when(report && null == value,  "Environment variable '%s' is not defined", name);
            return null == value ? "" : value;
        }
    }
}
