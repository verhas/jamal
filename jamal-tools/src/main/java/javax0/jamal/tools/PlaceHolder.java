package javax0.jamal.tools;

import java.util.Map;

/**
 * Replace the placeholders to the actual values in strings
 */
public class PlaceHolder {

    public static String replace(final String original, final Map<String, String> values) {
        StringBuilder sb = new StringBuilder(original);
        boolean repeatMore = true;
        int doneIndex = 0;
        while (repeatMore) {
            repeatMore = false;
            int min = Integer.MAX_VALUE;
            String key = "";
            String value = "";
            for (final var entry : values.entrySet()) {
                int pos = sb.indexOf(entry.getKey(),doneIndex);
                if (pos != -1 && pos < min) {
                    min = pos;
                    key = entry.getKey();
                    value = entry.getValue();
                }
            }
            if (min < sb.length()) {
                sb.replace(min, min + key.length(), value);
                repeatMore = true;
                doneIndex = min + value.length();
            }
        }
        return sb.toString();
    }
}
