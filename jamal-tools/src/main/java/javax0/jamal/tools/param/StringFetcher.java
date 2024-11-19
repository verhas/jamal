package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.tools.InputHandler;

import java.util.Objects;

import static javax0.jamal.tools.param.Escape.*;

public class StringFetcher {
    public static final String MULTI_LINE_STRING_DELIMITER = "\"\"\"";
    private static final int MLSD_LENGTH = MULTI_LINE_STRING_DELIMITER.length();
    private static final char ENCLOSING_CH = '"';

    public static String getString(Input input) throws BadSyntax {
        return getString(input, null);
    }

    public static String getString(Input input, Character terminal) throws BadSyntax {
        return getString(input, terminal, true);
    }

    public static String getString(Input input, Character terminal, boolean commaAllowed) throws BadSyntax {
        if (input.isEmpty() || input.charAt(0) != ENCLOSING_CH) {
            return getUnquotedString(input, terminal, commaAllowed);
        }
        BadSyntax.when(input.length() < 2, "String has to be at least two characters long.");
        if (input.length() >= MLSD_LENGTH && input.subSequence(0, MLSD_LENGTH).equals(MULTI_LINE_STRING_DELIMITER)) {
            return getMultiLineString(input);
        } else {
            return getSimpleString(input);
        }
    }

    private static String getUnquotedString(Input input, Character terminal, boolean commaAllowed) throws BadSyntax {
        final var output = new StringBuilder();
        while (true) {
            if (input.isEmpty() || Character.isWhitespace(input.charAt(0)) || Objects.equals(input.charAt(0), terminal)) {
                break;
            }
            if( !commaAllowed && input.charAt(0) == ','){
                break;
            }
            BadSyntax.when(input.charAt(0) == '=', "Unquoted parameters must not contain '='. It is dangerous.");
            output.append(input.charAt(0));
            InputHandler.skip(input, 1);
        }
        return output.toString();
    }

    private static String getMultiLineString(Input input) throws BadSyntax {
        final var output = new StringBuilder();
        InputHandler.skip(input, MLSD_LENGTH);
        while (input.length() >= MLSD_LENGTH && !input.subSequence(0, MLSD_LENGTH).equals(MULTI_LINE_STRING_DELIMITER)) {
            final char ch = input.charAt(0);
            if (ch == '\\') {
                handleEscape(input, output);
            } else {
                handleNormalMultiLineStringCharacter(input, output);
            }
        }
        BadSyntax.when(input.length() < MLSD_LENGTH, "Multi-line string is not terminated before eof");
        InputHandler.skip(input, MLSD_LENGTH);
        return output.toString();
    }

    private static String getSimpleString(Input input) throws BadSyntax {
        final var output = new StringBuilder();
        input.deleteCharAt(0);
        while (input.length() > 0 && input.charAt(0) != ENCLOSING_CH) {
            final char ch = input.charAt(0);
            if (ch == '\\') {
                handleEscape(input, output);
            } else {
                handleNormalCharacter(input, output);
            }
        }
        BadSyntax.when(input.length() == 0, "String is not terminated before eol");
        input.deleteCharAt(0);
        return output.toString();
    }
}
