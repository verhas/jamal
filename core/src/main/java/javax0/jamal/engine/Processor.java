package javax0.jamal.engine;

import java.util.Optional;
import java.util.regex.Pattern;

public class Processor {
    final static private int DOES_NOT_CONTAIN = -1;
    private String macroOpen;
    private String macroClose;

    public Processor(String macroOpen, String macroClose) {
        this.macroOpen = macroOpen;
        this.macroClose = macroClose;
    }

    public String process(final String in) {
        final var output = new StringBuilder();
        final var input = new StringBuilder(in);
        while (input.length() > 0) {
            if (input.indexOf(macroOpen) == 0) {
                processMacro(output, input);
            } else {
                processText(output, input);
            }
        }
        return output.toString();
    }

    /**
     * Process the text at the start of input till the first macro start.
     *
     * @param output where the text is appended
     * @param input  where the text is read from and removed after wards
     */
    private void processText(StringBuilder output, StringBuilder input) {
        int nextMacroStart = input.indexOf(macroOpen);
        if (-1 < nextMacroStart) {
            output.append(input.substring(0, nextMacroStart));
            skip(input, nextMacroStart);
        } else {
            output.append(input);
            input.setLength(0);
        }
    }

    /**
     * Process that macro that starts at the first character of the input.
     *
     * @param output where the processed macro is appended
     * @param input  from where the macro source is read and removed
     */
    private void processMacro(StringBuilder output, StringBuilder input) {
        skip(input, macroOpen);
        var macro = getNextMacroBody(input);
        if (!firstNonSpaceIs(macro, '@')) {
            macro = process(macro);
        }
        output.append(evalMacro(macro));
    }

    private boolean firstNonSpaceIs(String s, char c) {
        int i = firstNonSpace(s);
        return i != DOES_NOT_CONTAIN && s.charAt(i) == c;
    }

    private int firstNonSpace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private void skip(StringBuilder input, int numberOfCharacters) {
        input.delete(0, numberOfCharacters);
    }

    private void skip(StringBuilder input, String s) {
        final var len = s.length();
        skip(input, s.length());
    }


    /**
     * Evaluate a macro. Either user defined macro, built in or otherwise defined.
     *
     * @param macro the macro text to be processed without the opening and closing string.
     * @return
     */
    Optional<String> evalMacro(final String macro) {
        final var input = new StringBuilder(macro);
        skipWhiteSpaces(input);
        final boolean reportUndef;
        if (macroIsBuiltIn(input)) {
            skip(input, 1);
            skipWhiteSpaces(input);
            reportUndef = true;
        } else {
            if (reportUndef = (input.charAt(0) == '?')) {
                skip(input, 1);
                skipWhiteSpaces(input);
            }
            var id = fetchId(input);
            if (id.charAt(0) == '$') {
                // handle loop variables
            } else {
                if (id.length() == 0) {
                    return Optional.empty();
                }
                skipWhiteSpaces(input);
                var separator = input.substring(0, 1);
                skip(input, 1);
                String[] parameters = input.toString().split(Pattern.quote(separator));

            }
        }

        return Optional.of(macro);
    }

    /**
     * decides if a macro is user defined or build in. The decision is fairly simple
     * because built in macros start with {@code #} or {@code @} character.
     *
     * @param input containing the macro starting at the position zero
     * @return {@code true} if the macro is a built in macro and {@code false} if the macro is user defined
     */
    private boolean macroIsBuiltIn(StringBuilder input) {
        return input.charAt(0) == '#' || input.charAt(0) == '@';
    }


    private String fetchId(StringBuilder input) {
        final var output = new StringBuilder();
        while (input.length() > 0 && vaidIdChar(input.charAt(0))) {
            output.append(input.charAt(0));
            skip(input, 1);
        }
        return output.toString();
    }

    private boolean vaidIdChar(char c) {
        return c == '$' || c == '_' || c == ':' || Character.isAlphabetic(c) || Character.isDigit(c);
    }

    private void skipWhiteSpaces(StringBuilder input) {
        while (input.length() > 0 && Character.isWhitespace(input.charAt(0))) {
            input.delete(0, 1);
        }
    }

    String getNextMacroBody(final StringBuilder input) {

        var counter = 1; // we are after one macro opening, so that counts as one opening
        final var output = new StringBuilder();

        while (counter > 0) {// while there is any opened macro
            if (input.length() == 0) {// some macro was not closed
                //&Error('Erroneous macro nesting.',$output);
                return output.toString();
            }

            if (input.indexOf(macroOpen) == 0) {
                moveMacroOpenToOutput(input, output);
                counter++; //count the new opening
            } else if (input.indexOf(macroClose) == 0) {
                counter--; // count the closing
                if (counter == 0) {
                    skip(input, macroClose);
                    return output.toString();
                } else {
                    moveMacroCloseToOutput(input, output);
                }
            } else {
                var start = input.indexOf(macroOpen);
                var close = input.indexOf(macroClose);
                if (close < start && close != DOES_NOT_CONTAIN || start == DOES_NOT_CONTAIN) {
                    start = close;
                }
                if (start == DOES_NOT_CONTAIN) {
                    output.append(input);
                    input.setLength(0);
                } else {
                    output.append(input.substring(0, start));
                    skip(input, start);
                }
            }
        }
        return output.toString();
    }

    private void moveMacroCloseToOutput(StringBuilder input, StringBuilder output) {
        moveStringToOutput(input, output, macroClose);
    }

    private void moveMacroOpenToOutput(StringBuilder input, StringBuilder output) {
        moveStringToOutput(input, output, macroOpen);
    }

    private void moveStringToOutput(StringBuilder input, StringBuilder output, String s) {
        skip(input, s);
        output.append(s);
    }


}
