package javax0.jamal.engine.util;

import java.util.Map;
import java.util.regex.Pattern;

public class Replacer {
    private final Map<String, String> map;
    private final String openStr;
    private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\s*(\\@|\\#)\\s*escape\\s*(`.*?`).*?\\2\\s*");

    public Replacer(Map<String, String> map, String openStr) {
        this.map = map;
        this.openStr = openStr;
    }

    private final StringBuilder sb = new StringBuilder();
    private String key, value;
    private int from;

    public String replace(String input) {
        sb.delete(0, sb.length());
        sb.append(input);
        int loc = 0;
        while (find(loc)) {
            loc = replace();
        }
        return sb.toString();
    }

    private int replace() {
        sb.replace(from, from + key.length(), value);
        if (key.equals(openStr)) {
            final var matcher = ESCAPE_PATTERN.matcher(sb.substring(from+value.length()));
            if( matcher.find() && matcher.start() == 0 ){
                return from + value.length() + matcher.end();

            }
        }
        return from + value.length();
    }

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
