package javax0.jamal.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A parameter parser that is to parse the command line and after that to query the command parameters.
 */
public class CmdParser {

    private final Map<String, String> keys = new HashMap<>();
    private final List<String> values = new ArrayList<>();

    /**
     * Parse a string and build up the parsed structures.
     * <p>
     * A line can have the form
     * <p>
     * {@code
     * key1=value1 key2=value2 .... parameter ... key3=value3 ... parameter
     * }
     * <p>
     * The keys have to be unique, so no one key can be used twice. The parameters that stand alone on the line
     * without an associated key can be mixed between, before and after the keys.
     * <p>
     * The keys, in case they are defined in the argument {@code parameters} are case-insensitive.
     *
     * @param parts      the line that contains the keys and also the arguments
     * @param parameters a set of parameter names that are allowed on the line.
     *                   In case this parameter is null, any parameter is allowed.
     *                   If this set is empty, then no parameter is allowed.
     *                   If the set contains
     *                   the parameter names, then the line may use any non-ambiguous prefix of any parameter, and the
     *                   parsed structure will contain the full parameter name even if the user typed a short prefix.
     * @return the parsed structure object that can later be queried
     * @throws IllegalArgumentException if the line is not properly formatted
     */
    public static CmdParser parse(String[] parts, Set<String> parameters) {
        final var it = new CmdParser();
        for (var part : parts) {
            var partIsKey = false;
            while (part.startsWith("-")) {
                part = part.substring(1);
                partIsKey = true;
            }
            final var eq = part.indexOf("=");
            if (eq == -1) {
                if (part.length() > 0) {
                    if (partIsKey) {
                        it.keys.put(findIt(part, parameters), "true");
                    } else {
                        it.values.add(part);
                    }
                }
            } else {
                final var key = part.substring(0, eq);
                final var value = part.substring(eq + 1);
                final var realKey = findIt(key, parameters);
                if( it.keys.containsKey(realKey)) {
                    throw new IllegalArgumentException("The key " + realKey + " is defined more than once");
                }
                it.keys.put(realKey, value);
            }
        }
        return it;
    }

    /**
     * Find the key in the usable parameters' set. The key matches a parameter if it is a prefix of the parameter.
     * If there are more than one parameter that matches the key then it is an error. The user has to specify an
     * unambiguous prefix of the parameter.
     *
     * @param key the key the user entered
     * @param set the set of parameters that are allowed on the line
     * @return the full name of the parameter that was found
     */
    private static String findIt(String key, Set<String> set) {
        if (set == null) {
            return key;
        }
        final List<String> commandsFound = new ArrayList<>();
        for (final var s : set) {
            if (s.toLowerCase().startsWith(key.toLowerCase())) {
                commandsFound.add(s);
            }
        }
        if (commandsFound.size() == 1) {
            return commandsFound.get(0);
        }
        if (commandsFound.size() == 0) {
            throw new IllegalArgumentException(key + " is not an allowed parameter");
        }
        throw new IllegalArgumentException("Parameter " + key + " is ambiguous. " +
                "It matches " + String.join(",", commandsFound) + ".");
    }

    /**
     * Get the value that was associated with the key on the parsed line.
     *
     * @param key the full key as it was defined in the possible keys set
     * @return the optional value as it was on the line or {@code Optional.empty()}
     * in case the key was not present on the line
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(keys.get(key));
    }

    /**
     * Get the {@code i}-th parameter (starting with 0) from the parsed line. In case {@code i} is larger than
     * the index of the last parameter {@code Optional.empty()} is returned.
     *
     * @param i index of the parameter
     * @return the parameter from the line or {@code Optional.empty()} if {@code i} is too large
     */
    public Optional<String> get(int i) {
        if (i < values.size()) {
            return Optional.ofNullable(values.get(i));
        } else {
            return Optional.empty();
        }
    }
}
