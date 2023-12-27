package javax0.jamal.debugger;

import java.util.List;
import java.util.Map;

/**
 * An extremely simple JSON converter for the HTTP debugger.
 */
public class JsonConverter {
    /*
     * Note about why I do not use a well-established, external JSON library:
     * <p>
     * When you are developing an application, you should usually opt for the use of a library. Writing a simple class
     * like this takes no more than 30 minutes at the start. In an application, however, there comes a time when you
     * need more and more features. At a certain point in time, the cumulated maintenance effort will become more than
     * the difference between the initial 30min development time of the simple class and tasks needed to utilize an
     * external library. These are roughly careful library research, selection, learning, integration and library
     * version change, and security screening and follow-up.
     * <p>
     * The two costs considering the Total Cost of Ownership have to be carefully measured. Using the external library
     * almost always wins.
     * <p>
     * When you are developing a library, you should consider the same costs. However, there is a multiplication factor
     * when you think of the library version change and security screening and follow-up in this case. This part of the
     * cost, which uses an external library heavier, should be multiplied by the number of installation users will
     * utilize your library. It means that you should consider using an external library
     * <li>if you intend to use a lot of features (the development is not 30min),
     * <li>you expect the library to be highly stable, rarely changing, not even for security reasons,
     * <li>you wish only a very few users for your library.
     */
    static String object2Json(Object in) {
        final var sb = new StringBuilder();
        if (in instanceof Map) {
            final var map = (Map<?, ?>) in;
            String sep = "";
            sb.append("{");
            for (final var e : map.entrySet()) {
                sb.append(sep);
                sb.append("\"");
                sb.append(escape(e.getKey().toString()));
                sb.append("\": ");
                sb.append(object2Json(e.getValue()));
                sep = ", ";
            }
            sb.append("}");
            return sb.toString();
        }
        if (in instanceof List) {
            final var list = (List<?>) in;
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
