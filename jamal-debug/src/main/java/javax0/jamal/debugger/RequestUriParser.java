package javax0.jamal.debugger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class RequestUriParser {
    final String uri;
    final Map<String, String> params = new HashMap<>();
    final String queryString;
    final String context;

    /**
     * A simple parser for the URL of the request sent to the debugger.
     * The parsing cares only the query string and the context. It does not
     * parse the context into parts.
     *
     * The query string is parsed into key-value pairs. The value may be
     * empty string.
     *
     * The parsing also assumes that there are no repeated keys in the query.
     * @param uri the URI of the request
     * @return the parser object already containing the parsed data
     */
    static RequestUriParser parse(final String uri){
        return new RequestUriParser(uri);
    }

    private RequestUriParser(String requestUri) {
        uri = requestUri;
        final int qmPos = uri.indexOf('?');
        if (qmPos == -1) {
            queryString = "";
            context = uri;
        } else {
            if (qmPos < uri.length() - 1) {
                queryString = uri.substring(qmPos + 1);
            } else {
                queryString = "";
            }
            context = uri.substring(0, qmPos);
        }
        Arrays.stream(queryString.split("&", -1)).forEach(
            kv -> {
                final var kvp = kv.split("=", -1);
                params.put(kvp[0], kvp.length > 1 ? kvp[1] : "");
            }
        );
    }
}
