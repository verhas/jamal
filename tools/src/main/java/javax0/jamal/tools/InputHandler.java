package javax0.jamal.tools;

import java.util.function.Function;

public class InputHandler {
    final static private int DOES_NOT_CONTAIN = -1;

    public static boolean firstCharIs(CharSequence s, char c) {
        return s.charAt(0) == c;
    }

    public static boolean firstNonSpaceIs(CharSequence s, char c) {
        int i = firstNonSpace(s);
        return i != DOES_NOT_CONTAIN && s.charAt(i) == c;
    }

    public static int firstNonSpace(CharSequence s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }
        return -1;
    }


    public static void trim(String[] s) {
        convert(s, String::trim);
    }

    public static void convert(String[] s, Function<String, String> f) {
        for (int i = 0; i < s.length; i++) {
            s[i] = f.apply(s[i]);
        }
    }

    public static void skip(StringBuilder input, int numberOfCharacters) {
        input.delete(0, numberOfCharacters);
    }

    public static void skip(StringBuilder input, String s) {
        skip(input, s.length());
    }

    public static boolean contains(int i) {
        return i != DOES_NOT_CONTAIN;
    }


    public static String fetchId(StringBuilder input) {
        final var output = new StringBuilder();
        while (input.length() > 0 && validIdChar(input.charAt(0))) {
            output.append(input.charAt(0));
            skip(input, 1);
        }
        return output.toString();
    }

    public static boolean validId1stChar(char c) {
        return c == '$' || c == '_' || c == ':' || Character.isAlphabetic(c);
    }

    public static boolean validIdChar(char c) {
        return validId1stChar(c) || Character.isDigit(c);
    }

    public static void skipWhiteSpaces(StringBuilder input) {
        while (input.length() > 0 && Character.isWhitespace(input.charAt(0))) {
            input.delete(0, 1);
        }
    }

    public static void copy(StringBuilder input, StringBuilder output, String s) {
        skip(input, s);
        output.append(s);
    }
}
