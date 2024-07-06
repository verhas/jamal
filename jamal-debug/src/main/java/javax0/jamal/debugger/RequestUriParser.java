package javax0.jamal.debugger;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

class RequestUriParser {
    final String uri;
    final Map<String, String> params = new HashMap<>();
    final String queryString;
    final String context;

    /**
     * Create a parser and parse the {@code uri} into the context and the query string.
     * The returned object can be used to get the context and the query string from the
     * final fields. Since the fields are final, there are no getters, and they are
     * package private.
     * <p>
     * This class and the method are used only in the debugger.
     * The typical use should be
     * <pre>
     *     {@code
     *           request = RequestUriParser.parse(e.getRequestURI().toString());
     *           p = request.params.get("parameter");
     *           qs = request.queryString;
     *           c = request.context;
     *     }
     * </pre>
     *
     * <p>
     * The query string is parsed into key-value pairs.
     * Values may be empty strings.
     * <p>
     * The parsing also assumes that there are no repeated keys in the query.
     *
     * @param uri the URI of the request
     * @return the parser object already containing the parsed data
     */
    static RequestUriParser parse(final URI uri) {
        return new RequestUriParser(uri.toString());
    }

    private RequestUriParser(String requestUri) {
        uri = requestUri;
        final int questionMarkPosition = uri.indexOf('?');
        if (questionMarkPosition == -1) {
            queryString = "";
            context = uri;
        } else {
            if (isTheLastCharacter(questionMarkPosition)) {
                queryString = "";
            } else {
                queryString = uri.substring(questionMarkPosition + 1);
            }
            context = uri.substring(0, questionMarkPosition);
        }
        for (final var kv : queryString.split("&", -1)) {
            final var kvp = kv.split("=", -1);
            params.put(kvp[0], kvp.length > 1 ? kvp[1] : "");
        }
    }

    private boolean isTheLastCharacter(int questionMarkPosition) {
        return questionMarkPosition == uri.length() - 1;
    }
}
