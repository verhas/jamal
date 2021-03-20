package javax0.jamal.debugger;

import java.util.List;
import java.util.Map;

/**
 * An extremely simple JSON converter for the HTTP debugger
 */
public class JsonConverter {
    static String object2Json(Object in) {
        final var sb = new StringBuilder();
        if (in instanceof Map) {
            final var map = (Map<String, ?>) in;
            String sep = "";
            sb.append("{");
            for (final var e : map.entrySet()) {
                sb.append(sep);
                sb.append("\"");
                sb.append(escape(e.getKey()));
                sb.append("\": ");
                sb.append(object2Json(e.getValue()));
                sep = ", ";
            }
            sb.append("}");
            return sb.toString();
        }
        if (in instanceof List) {
            final var list = (List) in;
            String sep = "";
            sb.append("[");
            for (final var e : list) {
                sb.append(sep);
                sb.append(object2Json(e));
                sep = ", ";
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + escape(in.toString()) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\f", "\\f")
            .replace("\"", "\\\"");
    }
}
