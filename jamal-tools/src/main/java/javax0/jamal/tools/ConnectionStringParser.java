package javax0.jamal.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Parses a debugger connection string and stores the resulting parameters and options.
 * The format of a debugger connection string is:
 *
 * <pre>{@code
 *     protocol:param1:param2:param3?option1=1&&option2
 *     }
 * </pre>
 * The protocol, the parameters can be queried when the object was created.
 * The parsing is lenient, it does not care what options are given.
 * Any option is accepted.
 * Also, there is no api to query all the options.
 * The called has to query the options it wants to use and ignore the others.
 */
public class ConnectionStringParser {
    public final String[] parameters;
    public final String protocol;
    public final Map<String, String> options;

    /**
     * Get the parameter array.
     * @return the parameter array (not a copy).
     */
    public String[] getParameters() {
        return parameters;
    }

    /**
     * Get the protocol.
     * @return the protocol.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Get the option value for the given key.
     * @param key the name of the option.
     * @return the option value.
     */
    public Optional<String> getOption(String key) {
        return Optional.ofNullable(options.get(key));
    }

    public ConnectionStringParser(String uri) {
        final var p = uri.split(":", -1);
        protocol = p[0];
        final String optionsString = cutOffOptions(p);
        parameters = getParametersArray(p);
        options = parseOptions(optionsString);
    }

    /**
     * Cut off the options from the last element of the array of parameters.
     *
     * @param p the array of parameters.
     * @return the options string.
     */
    private static String cutOffOptions(final String[] p) {
        final var optIndex = p[p.length - 1].indexOf("?");
        if (optIndex == -1) {
            return "";
        } else {
            final var optionsString = p[p.length - 1].substring(optIndex + 1);
            p[p.length - 1] = p[p.length - 1].substring(0, optIndex);
            return optionsString;
        }
    }

    private String[] getParametersArray(final String[] p) {
        final String[] parameters;
        if (p.length > 1) {
            parameters = Arrays.copyOfRange(p, 1, p.length);
        } else {
            parameters = new String[0];
        }
        return parameters;
    }

    /**
     * Parse the options from the options string. The options are separated by '&' and the key and value are separated by '='.
     *
     * @param optionsString the options string to be parsed.
     * @return a map of the options.
     */
    private Map<String, String> parseOptions(final String optionsString) {
        if (optionsString.isEmpty()) {
            return Map.of();
        }
        final Map<String, String> options;
        final var pairs = optionsString.split("&", -1);
        options = new HashMap<>();
        for (final var pair : pairs) {
            final var kv = pair.split("=", 2);
            if (kv.length > 0) {
                if (kv.length > 1) {
                    options.put(kv[0], kv[1]);
                } else {
                    options.put(pair, "");
                }
            }
        }
        return options;
    }
}
