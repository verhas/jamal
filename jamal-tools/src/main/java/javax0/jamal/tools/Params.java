package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.param.Param;
import javax0.jamal.tools.param.StringFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpacesNoNL;
import static javax0.jamal.tools.InputHandler.startsWith;

/**
 * Parse the start of the input for macro parameters.
 * <p>
 * Multi-line built-in macros, like {@code replaceLines} or {@code trimLines} get some parameters from the first line of
 * the input as well as from user defined macros. For built-in macros that need parameters this class helps with parsing
 * services. The use of the class is the following:
 * <pre>{@code
 * Params.using(processor).from(this).keys(Set.of(n1,h2,h3,...,hN)).parse(input)
 * }</pre>
 * <p>
 * This call parses the start of the input also consuming it. The parsing starts at the end of the first line. The
 * processor is used to fetch values from user defined macros in case the parameter is not defined on the line. The
 * parsing also checks that only {@code k1}, {@code k2}, {@code k3}, ... , {@code kn} keywords are used as parameters.
 * <p>
 * For example the macro trim line can be used as the following:
 *
 * <pre>{@code
 * [@trimLine margin=5
 *   content to be trimmed
 * ]
 * }</pre>
 * <p>
 * The implementation has to use the created {@code params} object to query the values. There are four different ways to
 * access the values, as documented in the JavaDoc of the public methods.
 * <p>
 * A parameter can present more than one time on the parsed line. In that case it is possible to query more values as
 * list. If multiple values are present then calling the single value return will throw an exception.
 * <p>
 * If a key is present on the input then the user defined macro of the same name is not used as value source.
 */
public class Params {
public interface Param<T> {
    String key();
    void inject(Processor processor, String macroName);
    void set(String value);
    Param<T> orElse(String i);
    Param<T> orElse(int i);
    Param<T> as(Function<String, T> converter);
    Param<Integer> asInt();
    Param<Boolean> asBoolean();
    Param<List<?>> asList();
    T get() throws BadSyntax;
}
    private final Processor processor;
    private Map<String,Param> holders = new HashMap<>();
    private String macroName = "undefined";
    private Character terminal = '\n';

    private Params(Processor processor) {
        this.processor = processor;
    }

    /**
     * Call this static method as the first in the chain to get an uninitialized params builder structure.
     *
     * @param processor the processor which is used to access user defined macros when querying data
     * @return a new uninitialized object needing parsing
     */
    public static Params using(Processor processor) {
        return new Params(processor);
    }

    /**
     * This method identifies the macro that uses the services of the class. Only the name of the macro is used for
     * error reporting purposes. The format of the use of this method is usually {@code from(this)}.
     *
     * @param macro the macro that is the caller object of this method
     * @return {@code this} for chaining
     */
    public Params from(Identified macro) {
        macroName = macro.getId();
        return this;
    }

    public Params till(char terminal) {
        this.terminal = terminal;
        return this;
    }

    public Params tillEnd() {
        this.terminal = null;
        return this;
    }

    public Params keys(Param<?>... holders) {
        for (final var holder : holders) {
            this.holders.put(holder.key(),holder);
        }
        return this;
    }

    public static <T> Param<T> holder(String key) {
        return new javax0.jamal.tools.param.Param<T>(key);
    }

    /**
     * Parse the input and collect the parameters in a map. The characters parsed are consumed from the input including
     * the last new-line character.
     * <p>
     * The parameters have the format
     * <pre>{@code
     * key=value
     * key="value"
     * key="""value"""
     * key
     * }</pre>
     * <p>
     * When the string starts with {@code """} it is a multi-line string. In this case the parsing does not stop at the
     * new line, which is inside the string.
     * <p>
     * New-line characters can als be escaped using a {@code \} character right before the new line outside of strings.
     * This can be used to increase the readability of the code in case there are many parameters for the macro.
     * <p>
     * String parsing implements all Java string features, including all escape sequences, octal number and unicode
     * escape sequences.
     *
     * @param input the input that starts with the parameters
     * @throws BadSyntax in many cases.
     *                   <ul>
     *                       <li>if a string is not terminated till the end of the input</li>
     *                       <li>if a {@code "} separated string is not terminated till the end of the first line</li>
     *                       <li>if there is a parameter, which is not listed in the keys</li>
     *                   </ul>
     */
    public void parse(Input input) throws BadSyntax {
        for (final var holder : holders.values()) {
            holder.inject(processor,macroName);
        }
        while ((terminal == null || !firstCharIs(input, terminal)) && input.length() > 0) {
            skipSpacesAndEscapedNL(input);
            if (terminal != null && firstCharIs(input, terminal)) {
                break;
            }
            final var id = fetchId(input);
            if (!holders.containsKey(id)) {
                throw new BadSyntax("The key '" + id + "' is not used by the macro '" + macroName + "'.");
            }
            final String param;
            skipSpacesAndEscapedNL(input);
            if (firstCharIs(input, '=')) {
                skip(input, 1);
                skipSpacesAndEscapedNL(input);
                param = StringFetcher.getString(input);
            } else {
                param = "true";
            }
            holders.get(id).set(param);
        }
        skip(input, 1);
    }

    private static void skipSpacesAndEscapedNL(Input input) {
        while (true) {
            skipWhiteSpacesNoNL(input);
            if (startsWith(input, "\\\n") != -1) {
                skip(input, 2);
            } else {
                return;
            }
        }
    }

}
