package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

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
public class Env implements Macro, Scanner.Core {
    public enum Mode {
        normal, query, report
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var mode = scanner.enumeration(Mode.class).defaultValue(Mode.normal);
        scanner.done();
        final var variableName = in.toString().trim();
        BadSyntax.when(variableName.isEmpty(), "Empty string as environment variable name");
        final var lastChar = variableName.charAt(variableName.length() - 1);
        final var testChar = lastChar == QUERY;
        final var reportChar = lastChar == REPORT_ERRMES;
        final var name = (testChar || reportChar) ? variableName.substring(0, variableName.length() - 1).trim() : variableName;

        final var value = EnvironmentVariables.getenv(name).orElse(null);
        if (testChar || mode.get(Mode.class) == Mode.query) {
            return "" + (null != value);
        } else {
            BadSyntax.when((mode.get(Mode.class)== Mode.report || reportChar) && null == value, "Environment variable '%s' is not defined", name);
            return null == value ? "" : value;
        }
    }
}
/*template jm_env
{template |env|env $P$ $C$|get the value of an environment variable|
  {variable |P|enum("?","!","")}
  {variable |C|"..."}
}
 */