package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.param.StringFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

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
 * final var params = Params.using(processor).from(this).keys(Set.of("k1","k2","k3",...,"kn")).parse(input)
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

    private final Map<String, List<String>> map = new HashMap<>();
    private final Processor processor;
    private final Set<String> allowedKeys = new HashSet<>();
    private String macroName = "undefined";

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

    /**
     * Define the set of keys that are allowed for this macro.
     *
     * @param allowedKeys the allowed key, which will be added to the set defined with earlier call to this method.
     * @return {@code this} for chaining
     */
    public Params keys(Set<String> allowedKeys) {
        this.allowedKeys.addAll(allowedKeys);
        return this;
    }

    /**
     * Get the value of a parameter as a string.
     *
     * @param key the key that we query for a single value
     * @return the single value in an optional or empty optional if the key was not present on the input and was not
     * defined in any user defined macro
     * @throws BadSyntax if there are multiple values for this key or if the key is not allowed for this macro. This
     *                   latter is most probably a coding error in the macro implementation. A caller should not query a
     *                   parameter, which it did not allow a few lines earlier in the call to the method {@link
     *                   #keys(Set)}
     */
    public Optional<String> get(String key) throws BadSyntax {
        assertKey(key);
        final List<String> values;
        if (map.containsKey(key) && (values = map.get(key)).size() > 0) {
            if (values.size() > 1) {
                throw new BadSyntax("The key '" + key + "' must not be multi valued in the macro '" + macroName + "'");
            }
            return Optional.ofNullable(values.get(0));
        }
        final var reader = MacroReader.macro(processor);
        return reader.readValue(key);
    }

    /**
     * @param key the key we are looking for
     * @return {@code true} if the key was defined on the input or as user defined macro, or as option
     * @throws BadSyntax when the underlying call to {@link #get(String)} throws
     */
    public boolean is(String key) throws BadSyntax {
        return get(key).map( s -> true).orElseGet( () -> OptionsStore.getInstance(processor).is("trimVertical"));
    }

    private void assertKey(String key) throws BadSyntax {
        if (!allowedKeys.contains(key)) {
            throw new BadSyntax("The key '" + key + "' is not allowed for the macro '"
                + macroName + "'. Macro internal error?");
        }
    }

    /**
     * @param key the key we are looking for
     * @return the possibly empty list of the values
     * @throws BadSyntax if there was no parameter defined with the name {@code key} and evaluating the user defined
     *                   macro of the same name throws up.
     */
    public List<String> getList(String key) throws BadSyntax {
        assertKey(key);
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return MacroReader.macro(processor).readValue(key).map(List::of).orElse(List.of());
    }

    /**
     * Gets the value assigned to the {@code key} calling {@link #get(String)} and converts it to an optional int.
     *
     * @param key the key we are looking for
     * @return optional int value of the parameter
     * @throws BadSyntax if the used }{@link #get(String)} throws up, or if the value cannot be converted to int
     */
    public OptionalInt getInt(String key) throws BadSyntax {
        final var string = get(key);
        if (string.isPresent()) {
            try {
                return OptionalInt.of(Integer.parseInt(string.get()));
            } catch (NumberFormatException nfe) {
                throw new BadSyntax(key + " is not a number using the macro '" + macroName + "'.");
            }
        } else {
            return OptionalInt.empty();
        }
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
     * @return {@code this} ready to query using the {@link #is(String)}, {@link #get(String)}, {@link #getInt(String)},
     * {@link #getList(String)} methods.
     * @throws BadSyntax in many cases.
     *                   <ul>
     *                       <li>if a string is not terminated till the end of the input</li>
     *                       <li>if a {@code "} separated string is not terminated till the end of the first line</li>
     *                       <li>if there is a parameter, which is not listed in the keys</li>
     *                   </ul>
     */
    public Params parse(Input input) throws BadSyntax {
        while (!firstCharIs(input, '\n') && input.length() > 0) {
            skipSpacesAndEscapedNL(input);
            if (firstCharIs(input, '\n')) {
                break;
            }
            final var id = fetchId(input);
            if (!allowedKeys.contains(id)) {
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
            map.computeIfAbsent(id, (x) -> new ArrayList<>()).add(param);
        }
        skip(input, 1);
        return this;
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
