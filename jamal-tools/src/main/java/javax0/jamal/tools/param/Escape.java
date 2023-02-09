package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;

class Escape {
    private static char octal(Input input, int maxLen) {
        int i = maxLen;
        int occ = 0;
        while (i > 0 && input.length() > 0 && input.charAt(0) >= '0' && input.charAt(0) <= '7') {
            occ = 8 * occ + input.charAt(0) - '0';
            input.deleteCharAt(0);
            i--;
        }
        return (char) occ;
    }

    private static final String escapes = "btnfr\"'\\";
    private static final String escaped = "\b\t\n\f\r\"'\\";

    /**
     * Handle the escape sequence. The escape sequence is
     *
     * <ul>
     *      <li>backslash and a 'b', 't', 'n', 'f', 'r', '"', '\' or an apostrophe, or</li>
     *      <li>backslash and 2 or 3 octal characters.</li>
     * </ul>
     *
     * @param input  the input string
     * @param output the output string where the escaped character is appended
     * @throws BadSyntax if the escape sequence is invalid
     */
    static void handleEscape(Input input, StringBuilder output) throws BadSyntax {
        input.deleteCharAt(0);
        BadSyntax.when(input.length() == 0, "Source ended inside a string.");
        final var nextCh = input.charAt(0);
        final int esindex = escapes.indexOf(nextCh);
        if (esindex == -1) {
            if (nextCh >= '0' && nextCh <= '3') {
                output.append(octal(input, 3));
            } else if (nextCh >= '4' && nextCh <= '7') {
                output.append(octal(input, 2));
            } else {
                throw new BadSyntax("Invalid escape sequence in string: \\" + nextCh);
            }
        } else {
            output.append(escaped.charAt(esindex));
            input.deleteCharAt(0);
        }
    }

    static void handleNormalCharacter(Input input, StringBuilder output) throws BadSyntax {
        final char ch = input.charAt(0);
        BadSyntax.when(ch == '\n' || ch == '\r', () -> String.format("String not terminated before eol:\n%s...",
                input.substring(1, Math.min(input.length(), 60))));
        output.append(ch);
        input.deleteCharAt(0);
    }

    static void handleNormalMultiLineStringCharacter(Input input, StringBuilder output) {
        char ch = input.charAt(0);
        if (ch == '\n' || ch == '\r') {
            normalizedNewLines(input, output);
        } else {
            output.append(ch);
            input.deleteCharAt(0);
        }
    }

    /**
     * <p>Convert many subsequent {@code \n} and {@code \r} characters to {@code \n} only. There will be as many {@code
     * \n} characters in the output as many there were in the input and the {@code \r} characters are simply ignored.
     * The only exception is, when there are no {@code \n} characters. In this case there will be one {@code \n} in the
     * output for all the {@code \r} characters.</p>
     *
     * <p>The method deletes the characters from the start of the input {@code input} and append the output
     * to the {@code output}. The original characters will be appended to the end of {@code original} without any
     * conversion.</p>
     *
     * @param input  the input, from which the characters are consumed.
     * @param output where the converted newline(s) are appended to
     */
    private static void normalizedNewLines(Input input, StringBuilder output) {
        char ch = input.charAt(0);
        int countNewLines = 0;
        while (input.length() > 0 && (ch == '\n' || ch == '\r')) {
            if (ch == '\n') {
                countNewLines++;
            }
            input.deleteCharAt(0);
            if (input.length() > 0) {
                ch = input.charAt(0);
            }
        }
        // if there was a single, or multiple \r without any \n
        if (countNewLines == 0) {
            countNewLines++;
        }
        output.append("\n".repeat(countNewLines));
    }

}
