package javax0.jamal.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConnectionStringParser {
    public final String[] parameters;
    public final String protocol;
    public final Map<String, String> options;


    public String[] getParameters() {
        return parameters;
    }

    public String getProtocol() {
        return protocol;
    }

    public Optional<String> getOption(String key) {
        return Optional.ofNullable(options.get(key));
    }

    public ConnectionStringParser(String uri) {
        final var p = uri.split(":", -1);
        if (p.length > 0) {
            protocol = p[0];
            final var optIndex = p[p.length - 1].indexOf("?");
            if (optIndex == -1) {
                options = Map.of();
                if (p.length > 1) {
                    parameters = Arrays.copyOfRange(p, 1, p.length);
                } else {
                    parameters = new String[0];
                }
            } else {
                final var optionsString = p[p.length - 1].substring(optIndex + 1);
                p[p.length - 1] = p[p.length - 1].substring(0, optIndex);
                if (p.length > 1) {
                    parameters = Arrays.copyOfRange(p, 1, p.length);
                } else {
                    parameters = new String[0];
                }
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
                    } else {
                        throw new IllegalArgumentException("The connection string '" + uri + "' is invalid.\n" +
                            "The option '" + pair + "' is faulty.");
                    }
                }
            }
        } else {
            parameters = new String[0];
            protocol = uri;
            options = Map.of();
        }
    }


}
