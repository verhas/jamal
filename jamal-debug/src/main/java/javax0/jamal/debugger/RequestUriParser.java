package javax0.jamal.debugger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class RequestUriParser {
    final String uri;
    final Map<String, String> params = new HashMap<>();
    final String queryString;
    final String context;

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
