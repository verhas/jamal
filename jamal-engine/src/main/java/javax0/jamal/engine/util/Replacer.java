package javax0.jamal.engine.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Search and replace keys to values in a string. It is used to transform macro opening and closing string to escape
 * protected macro opening and closing strings so that the user defined macros do not change the functionality when the
 * opening and closing strings are changed.
 */
public class Replacer {
    private final Map<String, String> map;
    private final String openStr;
    private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\s*(@|#)\\s*escape\\s*(`.*?`).*?\\2\\s*");

    public Replacer(Map<String, String> map, String openStr) {
        this.map = map;
        this.openStr = openStr;
    }

    private final StringBuilder sb = new StringBuilder();
    private String key, value;
    private int from;

    public String replace(String input) {
        sb.setLength(0);
        sb.append(input);
        int loc = 0;
        while (find(loc)) {
            loc = replace();
        }
        return sb.toString();
    }

    /**
     * Replace the key with the value at the location {@code from}. If the replaced key is the macro opening string
     * then it is checked if the macro is an escape macro. If it is the end is calculated using the escape macro
     * pattern. In other cases, the end points after the replacement.
     *
     * @return the position after the replacement
     */
    private int replace() {
        sb.replace(from, from + key.length(), value);
        if (key.equals(openStr)) {
            final var matcher = ESCAPE_PATTERN.matcher(sb.substring(from + value.length()));
            if (matcher.find() && matcher.start() == 0) {
                return from + value.length() + matcher.end();

            }
        }
        return from + value.length();
    }

    /**
     * Find the key of the {@code map} which appears in the {@code sb} at th earliest position.
     * If any of the keys is found then the return value is {@code true} and the {@code key}, {@code value}, and
     * {@code from} fields are set.
     *
     * @param start where the search starts
     * @return true if a key was found
     */
    private boolean find(int start) {
        int min = sb.length();
        for (final var entry : map.entrySet()) {
            final var index = sb.toString().indexOf(entry.getKey(), start);
            if (index > -1 && index < min || (index == min && entry.getKey().length() > key.length())) {
                key = entry.getKey();
                value = entry.getValue();
                from = index;
                min = index;
            }
        }
        return min < sb.length();
    }

}
