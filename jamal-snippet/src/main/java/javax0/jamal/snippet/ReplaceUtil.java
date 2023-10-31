package javax0.jamal.snippet;

import java.util.Arrays;

/**
 * A simple utility class to contain the code used by both replace and replaceLine macros.
 */
public class ReplaceUtil {

    /**
     * If the last element is empty, then the last element is removed from the array.
     *
     * @param strings is the original array
     * @return the original array or a chopped copy of the original array
     */
    public static String[] chop(String[] strings) {
        if (strings.length > 0 && strings[strings.length - 1].isEmpty()) {
            return Arrays.copyOf(strings, strings.length - 1);
        }
        return strings;
    }

    /**
     * Fetch the element from the string array. If the index is out of bound, then return empty string.
     * @param strings is the array of the strings
     * @param i the index of the element to fetch
     * @return the element or empty string
     */
    public static String fetchElement(String[] strings, int i) {
        return i < strings.length  ? strings[i] : "";
    }

}
