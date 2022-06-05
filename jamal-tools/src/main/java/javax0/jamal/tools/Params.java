package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.param.StringFetcher;
import javax0.levenshtein.Levenshtein;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
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
    public static class ExtraParams {
        private final Map<String, Param<?>> params = new HashMap<>();
        ;
    }

    public interface Param<T> {
        String[] keys();

        void copy(Param<?> p);

        void inject(Processor processor, String macroName);

        void set(String value);

        Param<T> orElse(String i);

        Param<T> orElseNull();

        Param<Integer> orElseInt(int i);

        Param<T> as(Function<String, T> converter);

        Param<Pattern> asPattern();

        <K> Param<K> asType(Class<K> type);

        <K> Param<K> as(Class<K> klass, Function<String, K> converter);

        Param<Integer> asInt();

        Param<Boolean> asBoolean();

        Param<String> asString();

        Param<List<?>> asList();

        <K> Param<List<K>> asList(Class<K> k);

        T get() throws BadSyntax;

        boolean is() throws BadSyntax;

        boolean isPresent() throws BadSyntax;

        /**
         * @return the name, which was actually used for the parameter. It is the same string as the name of the
         * parameter or one of the aliases.
         * <p>
         * If the parameter is multi-values then the name used the last time will be returned.
         */
        String name();

        /**
         * Set the name of the parameter, which was used. This is the original name of the parameter or one of the
         * aliases. This value can be queried later calling {@link #name()}.
         *
         * @param id the string that was used as the parameter name/id
         */
        void setName(String id);
    }

    private final Processor processor;
    private final Map<String, Param<?>> holders = new HashMap<>();
    private ExtraParams extraParams = null;
    private String macroName = null;
    // The character that signals the end of the parameters
    // by default it is `\n` which means that the parameters are occupying the first line
    // new-line, when processing parameters can also be escaped
    // many times this character is ')', for core macros ']'
    private Character terminal = '\n';
    // The character that starts the parameters
    // by default there is no such character, parameters just start on the input
    // in some cases this is '(', for core macros it is '['
    private Character start = null;

    private static class BadKey extends BadSyntax {
        final String key;

        BadKey(String key, String message) {
            super(message);
            this.key = key;
        }
    }

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

    public Params between(String seps) {
        Objects.requireNonNull(seps);
        if (seps.length() != 2) {
            throw new IllegalArgumentException("The argument to method 'between()' has to be a 2-character string. It was '" + seps + "'");
        }
        this.start = seps.charAt(0);
        this.terminal = seps.charAt(1);
        return this;
    }

    public Params startWith(char start) {
        this.start = start;
        return this;
    }

    public Params endWith(char terminal) {
        this.terminal = terminal;
        return this;
    }

    public Params tillEnd() {
        this.terminal = null;
        return this;
    }

    /**
     * Specify the parameter holders and also an extra parameter holders in case there is a parameter, which is not used
     * by this macro. These extra parameters may be passed to another marco that this macro may invoke and then that
     * macro, instead of the {@link #parse(Input)} method will invoke the {@link #parse(ExtraParams)} methods.
     *
     * @param extraParams the holder for the extra parameters. It has to be a variable, as it will be passed on to the
     *                    next macro in the chain. It can be created as {@code new Params.ExtraParams()}
     * @param holders     the parameter holders
     * @return {@code this}
     */
    public Params keys(ExtraParams extraParams, Param<?>... holders) {
        this.extraParams = extraParams;
        return keys(holders);
    }

    /**
     * Specify the parameter holders. When the parsing works it stores the values parsed into these parameter holders.
     * When there is a parameter for which there is no holder an error occurs.
     *
     * @param holders the parameter holders
     * @return {@code this}
     */
    public Params keys(Param<?>... holders) {
        for (final var holder : holders) {
            for (final var key : holder.keys()) {
                if (key != null && this.holders.containsKey(key)) {
                    throw new IllegalArgumentException(
                            "The key '" + key + "' is used multiple times in macro '" + macroName + "'");
                }
                if (key != null) {
                    this.holders.put(key, holder);
                }
            }
        }
        return this;
    }

    /**
     * Specify the keys that can be used to specify the value. There can be aliases. The first value is the one that can
     * also be used as a macro name, the rests are alias. If the first value is null then no macro can define the value
     * for this parameter.
     *
     * @param key the array of key and aliases
     * @param <T> the type of the parameter, can be {@code Integer}, {@code String} or {@code Boolean}
     * @return a new holder
     */
    public static <T> Param<T> holder(String... key) {
        Objects.requireNonNull(key);
        if (key.length == 0 || (key.length == 1 && key[0] == null)) {
            throw new IllegalArgumentException("Parameter holder has to have at least one name.");
        }
        for (int i = 1; i < key.length; i++) {
            if (key[i] == null) { // key[0] may be null, that is OK, no macro can define the parameter
                throw new IllegalArgumentException("Parameter alias names must not be null.");
            }
        }
        return new javax0.jamal.tools.param.Param<>(key);
    }

    /**
     * Parse the input and collect the parameters in a map. The characters parsed are consumed from the input including
     * the last new-line or other specified terminating character.
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
        try {
            if (extraParams == null) {
                parse(input, (id, param) -> {
                    holders.get(id).set(param);
                    holders.get(id).setName(id);
                }, holders::containsKey);
            } else {
                parse(input, (id, param) -> {
                    getHolder(id).set(param);
                    getHolder(id).setName(id);
                }, (id) -> true);

            }
        } catch (BadKey e) {
            final var suggestions = suggest(e.key, holders.keySet());
            throw throwExceptionWithSuggestions(suggestions, e);
        }
    }

    private Params.Param<?> getHolder(final String id) {
        return holders.computeIfAbsent(id,
                (xId -> extraParams.params.computeIfAbsent(xId,
                        javax0.jamal.tools.param.Param::new)));
    }

    public void parse(ExtraParams extraParams) throws BadSyntax {
        parse();
        for (final var e : extraParams.params.entrySet()) {
            final var id = e.getKey();
            if (!holders.containsKey(id)) {
                if (this.extraParams != null) {
                    getHolder(id).copy(e.getValue());
                } else {
                    final var suggestions = suggest(id, holders.keySet());
                    throw throwExceptionWithSuggestions(suggestions,
                            new BadKey(e.getKey(), "The key '" + id + "' is not used by the macro '" + macroName + "'."));
                }
            }
            holders.get(e.getKey()).copy(e.getValue());
        }
    }

    /**
     * This is a general purpose version of the parameter handling. This method parses the parameters and fills them
     * into a LinkedHashMap. It is guaranteed that the keys are in the same order as they are in the input.
     * <p>
     * This version of the parsing was added to support parameter parsing of XML tag attributes, which is the same
     * syntax as the parameters. In this version the parsed values are returned as a map and the holders of the
     * {@code Params} are not used.
     *
     * @param input the input that contains the parameters
     * @return the linked hash map with the parameters
     * @throws BadSyntax in the same cases as {@link #parse(Input)}
     */
    public LinkedHashMap<String, String> fetchParameters(Input input) throws BadSyntax {
        final var parameters = new LinkedHashMap<String, String>();
        try {
            parse(input, parameters::put, id -> !parameters.containsKey(id));
        } catch (BadKey e) {
            final var suggestions = suggest(e.key, parameters.keySet());
            throw throwExceptionWithSuggestions(suggestions, e);
        }
        return parameters;
    }

    /**
     * Throws a BadSyntax exception fetching the erroneous key from the {@link BadKey} exception and composing a
     * suggestion set.
     * <p>
     * If the suggestions set is empty, the exception is thrown without suggestions.
     *
     * @param suggestions the set of suggestions
     * @param e           the BadKey exception
     * @return does not return, but declared to return a BadSyntax exception, so that the caller can "throw" it, and
     * it helps the data flow analysis of the compiler.
     * @throws BadSyntax is the composed exception
     */
    private BadSyntax throwExceptionWithSuggestions(final Set<String> suggestions, final BadKey e) throws BadSyntax {
        if (suggestions.isEmpty()) {
            throw e;
        } else {
            throw new BadSyntax("The key '"
                    + e.key
                    + "' is not used by the macro '" + macroName
                    + "'. Did you mean "
                    + suggestions
                    .stream()
                    .map(s -> "'" + s + "'")
                    .collect(Collectors.joining(", ")) + "?");
        }
    }

    /**
     * Get the suggestions that are similar to the given key.
     *
     * @param spelling the misspelled key
     * @param keys     the set of the valid keys
     * @return the set of suggestions
     */
    private Set<String> suggest(String spelling, Set<String> keys) {
        final Set<String> suggestions = new HashSet<>();
        int minDistance = 3;
        for (String key : keys) {
            final int distance = Levenshtein.distance(key, spelling);
            if (distance < minDistance) {
                minDistance = distance;
                suggestions.clear();
            }
            if (distance <= minDistance) {
                suggestions.add(key);
            }
        }
        return suggestions;
    }

    public void parse(Input input, BiConsumer<String, String> store, Predicate<String> valid) throws BadSyntax {
        parse();
        skipStartingSpacesAndEscapedTerminal(input);
        if (start != null) {
            if (firstCharIs(input, start)) {
                skip(input, 1);
            } else {
                return;
            }
        }
        while ((terminal == null || !firstCharIs(input, terminal)) && input.length() > 0) {
            if (terminal != null && firstCharIs(input, terminal)) {
                break;
            }
            skipSpacesAndEscapedTerminal(input);
            final var id = fetchId(input);
            if (!valid.test(id)) {
                throw new BadKey(id, "The key '" + id + "' is not used by the macro '" + macroName + "'.");
            }
            final String param;
            skipSpacesAndEscapedTerminal(input);
            if (firstCharIs(input, '=')) {
                skip(input, 1);
                skipSpacesAndEscapedTerminal(input);
                param = StringFetcher.getString(input, terminal);
            } else {
                param = "true";
            }
            store.accept(id, param);
            skipSpacesAndEscapedTerminal(input);
        }
        skip(input, 1);
    }

    public void parse() {
        for (final var holder : holders.values()) {
            holder.inject(processor, macroName);
        }
    }

    /**
     * Skip all the characters for which the {@code skipper} predicate returns true.
     * If the next character after all the skipped characters are {@code \} and {@code \n} (escaped new line) then
     * step over these and start the skipping again.
     * <p>
     * Skipping a character means deleting it from the front of theinput.
     *
     * @param input   to input to delete the characters from
     * @param skipper the predicate deciding if a chavater is to be skipped or not
     */
    private static void skipper(Input input, Predicate<Input> skipper) {
        while (true) {
            while (skipper.test(input)) {
                input.delete(1);
            }
            if (startsWith(input, "\\\n") != -1) {
                skip(input, 2);
            } else {
                return;
            }
        }

    }

    /**
     * Skip all characters, also new line characters, except when the terminal character is newline. In that case
     * the terminal character (which is a new line) stops the skipping. This method is used to eat the extra characters
     * between parameters.
     *
     * @param input the input that contains the next parameters with spaces optionally in front of them
     */
    private void skipSpacesAndEscapedTerminal(Input input) {
        skipper(input, i -> i.length() > 0 && Character.isWhitespace(i.charAt(0)) && !Objects.equals(i.charAt(0), terminal));
    }

    /**
     * Skip all the white space characters except new line. This method is invoked before the parameters.
     * An \ escaped newline character does not stop the skipping.
     *
     * @param input that contains the parameters with spaces optionally in front of them
     */
    private void skipStartingSpacesAndEscapedTerminal(Input input) {
        skipper(input, i -> i.length() > 0 && Character.isWhitespace(i.charAt(0)) && !Objects.equals(i.charAt(0), '\n'));
    }
}
