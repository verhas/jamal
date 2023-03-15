package javax0.jamal.tools;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Position;
import javax0.jamal.api.SpecialCharacters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static javax0.jamal.api.Macro.validId1stChar;
import static javax0.jamal.api.Macro.validIdChar;

/**
 * Utility class with some simple static methods that fetch characters from an input buffer.
 */
public class InputHandler {
    final static private int DOES_NOT_CONTAIN = -1;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];


    private InputHandler() {
    }

    /**
     * Checks that the first character of the input is one of the characters listed.
     *
     * @param s     a character sequence of which the first character is checked
     * @param chars the characters we are looking for
     * @return {@code true} if the first character of {@code s} is one of the {@code chars}. Returns {@code false} if
     * the character sequence is empty or the first character is none of the {@code chars}.
     */
    public static boolean firstCharIs(CharSequence s, char... chars) {
        if (s.length() == 0) {
            return false;
        }
        for (final var c : chars) {
            if (s.charAt(0) == c) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param s       a character sequence of which the start is checked
     * @param strings the possible strings to check that the sequence starts with
     * @return the index of the string that the sequence starts with or -1 if the sequence does not start with any of
     * the strings
     */
    public static int startsWith(CharSequence s, String... strings) {
        int i = 0;
        for (final var string : strings) {
            if (s.length() >= string.length() && s.subSequence(0, string.length()).equals(string)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * Delete the start of the input. It is an error trying to delete more character than the number of characters there
     * are in the input.
     *
     * @param input              from which the first characters are deleted
     * @param numberOfCharacters the number of characters to be deleted from the start of {@code input}
     */
    public static void skip(Input input, int numberOfCharacters) {
        input.delete(numberOfCharacters);
    }

    /**
     * Same as {@link #skip(Input, int)} but it also appends the deleted characters to the string builder.
     *
     * @param input              from which the first characters are deleted
     * @param numberOfCharacters the number of characters to be deleted from the start of {@code input}
     * @param sb                 where the characters will be appended
     * @deprecated Use the method {@link #move(Input, int, Input)} method instead. The interface `Input` extends {@link
     * CharSequence} and can give access to an underlying {@link StringBuilder}. Instead of creating a {@link
     * StringBuilder}, invoke {@link javax0.jamal.tools.Input#makeInput()} and use that instead of the {@link
     * StringBuilder}.
     * <p>
     * The reason to deprecate this method is to avoid use when the characters are first moved to a {@link
     * StringBuilder} and then a new {@link Input} is created from the {@link StringBuilder}. This approach may loose
     * the position information that later implementations of {@link InputHandler#move(Input, int, Input)} may also
     * copy.
     */
    @Deprecated
    public static void move(Input input, int numberOfCharacters, StringBuilder sb) {
        sb.append(input.substring(0, numberOfCharacters));
        input.delete(numberOfCharacters);
    }

    /**
     * Same as {@link #skip(Input, int)} but it also appends the deleted characters to the output.
     *
     * @param input              from which the first characters are deleted
     * @param numberOfCharacters the number of characters to be deleted from the start of {@code input}
     * @param output             where the characters will be appended
     */
    public static void move(Input input, int numberOfCharacters, Input output) {
        output.append(input.substring(0, numberOfCharacters));
        input.delete(numberOfCharacters);
    }

    /**
     * Copy the string from the start of the input to the end of the output.
     * <p>
     * Note that this is a convenience method for {@link #move(Input, int, Input)}. There is no check that the input
     * really starts with the characters contained by {@code s}.
     *
     * @param input  from which the string will be removed
     * @param s      the string. There is no check that the input really starts with the string. The string is {@link
     *               #skip(Input, String)}-ped in the {@code input} and is appended to the output.
     * @param output to which the string will be appended
     */
    public static void move(Input input, String s, Input output) {
        move(input, s.length(), output);
    }

    /**
     * Delete the start of the input.
     *
     * @param input from which the first characters are deleted
     * @param s     is a string that is supposed to be on the start of the input and this string is going to be deleted
     *              from the start of the {@code input}. The actual implementation does not check that the string is
     *              really there at the start of the input, it just skips so many characters as many the string has.
     */
    public static void skip(Input input, String s) {
        skip(input, s.length());
    }

    /**
     * @param i the result of {@link String#indexOf(int)}
     * @return {@code true} the value is a valid character code and not a signal that the string does not contain the
     * character we are looking for.
     */
    public static boolean contains(int i) {
        return i != DOES_NOT_CONTAIN;
    }

    /**
     * Fetch an id from the start of the {@code input}.
     * <p>
     * An identifier is a string that starts with a character accepted by {@link javax0.jamal.api.Macro#validId1stChar(char)} and contain only
     * characters that are accepted {@link javax0.jamal.api.Macro#validIdChar(char)}
     * <p>
     * or
     * <p>
     * a string that starts with some special character, which usually can not be part of an identifier and does not
     * contain space. This way you can have macros like
     * <pre>{@code
     *
     *        {@define =hatto (x)=belxanto}{#define {=hatto /1}(x) =tttxttt}{bel1anto/_}
     *
     * }</pre>
     * which is an experimental feature and is deliberately not documented except here. (So much not documented that
     * I am just watching and debugging it five years later... realising that I did that deliberately.)
     *
     * Just turned out that this is a very useful feature. It is used to use emojis as macro names... Who would have
     * thought?
     *
     * @param input that contains the identifier at the start. The identifier will be removed at the end of the method.
     * @return the identifier string that was found and removed from the start of the input.
     */
    public static String fetchId(Input input) {
        final var output = new StringBuilder();
        if (input.length() > 0 && validId1stChar(input.charAt(0))) {
            while (input.length() > 0 && validIdChar(input.charAt(0))) {
                output.append(input.charAt(0));
                skip(input, 1);
            }
        } else {
            while (input.length() > 0 && !Character.isWhitespace(input.charAt(0))) {
                output.append(input.charAt(0));
                skip(input, 1);
            }
        }
        return output.toString();
    }

    public static String fetchNumber(Input input) {
        final var output = new StringBuilder();
        while (input.length() > 0 && Character.isDigit(input.charAt(0))) {
            output.append(input.charAt(0));
            skip(input, 1);
        }
        return output.toString();
    }

    /**
     * Checks that the identifier is global or not. The check simply looks for embedded '{@code :}' character in the
     * identifier.
     *
     * @param id the identifier to check
     * @return {@code true} if the identifier is a global identifier.
     */
    public static boolean isGlobalMacro(String id) {
        return id.contains(":");
    }

    /**
     * Convert a global macro name.
     * <p>
     * Macro names that contain the '{@code :}' character are global macros and automatically are defined on the top
     * level. This provides a way to macro package developers to use name spacing, although Jamal does not handle name
     * spaces, the names can be treated as 'namespace:localName' or even name space notations can be nested.
     * <p>
     * A global macro without name space starts with a '{@code :}' character when defined but this character is removed
     * by this conversion so later the macro can be referred to with the name without the {@code :} character.
     *
     * @param id the identifier of the macro.
     * @return the converted identifier.
     */
    public static String convertGlobal(String id) {
        if (id.length() > 0 && id.charAt(0) == SpecialCharacters.GLOBAL_NAME_CHAR) {
            return id.substring(1);
        } else {
            return id;
        }
    }

    /**
     * Delete the white space characters from the start of the input
     *
     * @param input from which the spaces should be deleted.
     */
    public static void skipWhiteSpaces(Input input) {
        int i = 0;
        while (input.length() > i && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        input.delete(i);
    }

    /**
     * Delete the white space characters from the start of the input but do not delete {@code \n} characters. If it sees
     * a {@code \n} character then stop with the deletion.
     *
     * @param input from which the spaces (but no {@code \n})should be deleted.
     */
    public static void skipWhiteSpacesNoNL(Input input) {
        while (input.length() > 0 && Character.isWhitespace(input.charAt(0)) && input.charAt(0) != '\n') {
            input.delete(1);
        }
    }

    /**
     * Delete the white space characters from the start of the input but only until after the first EOL
     *
     * @param input from which the spaces should be deleted.
     */
    public static void skipWhiteSpaces2EOL(Input input) {
        skipWhiteSpacesNoNL(input);
        if (input.length() > 0 && input.charAt(0) == '\n') {
            input.delete(1);
        }
    }

    /**
     * Same as {@link #skipWhiteSpaces(Input)} but it also appends the deleted spaces to the string builder.
     *
     * @param input from which the spaces should be deleted.
     * @param sb    where the spaces will be appended
     */
    public static void moveWhiteSpaces(Input input, StringBuilder sb) {
        while (input.length() > 0 && Character.isWhitespace(input.charAt(0))) {
            sb.append(input.charAt(0));
            input.delete(1);
        }
    }

    /**
     * Same as {@link #skipWhiteSpaces(Input)} but it also appends the deleted spaces to the output.
     *
     * @param input  from which the spaces should be deleted.
     * @param output where the spaces will be appended
     */
    public static void moveWhiteSpaces(Input input, Input output) {
        while (input.length() > 0 && Character.isWhitespace(input.charAt(0))) {
            output.append(input.charAt(0));
            input.delete(1);
        }
    }

    /**
     * Delete the white space character from the start and from the end of the input.
     *
     * @param input from which the spaces should be deleted.
     */
    public static void trim(Input input) {
        skipWhiteSpaces(input);
        rtrim(input);
    }

    /**
     * Delete the white space character from the end of the input.
     *
     * @param input from which the spaces should be deleted.
     */
    public static void rtrim(Input input) {
        int i = input.length() - 1;
        while (i >= 0 && Character.isWhitespace(input.charAt(i))) {
            input.deleteCharAt(i);
            i--;
        }
    }

    /**
     * Delete the white space characters from the start of the input up to and including the next new-line character,
     * but only if there is a new-line character following zero or more non-new-line white space characters and the very
     * first character IS a back-slash {@code \}.
     * <p>
     * This method is used when the option {@code nl} is in effect that says that any new line character that follows a
     * macro closing string should be consumed and not put into the output. This helps writing better looking output
     * easier and not caring too much about the new lines.
     * <p>
     * If there are some spaces immediately before the new-line they will also be deleted, because they cannot easily be
     * recognized by the person editing the file and we want to avoid mysterious errors.
     *
     * @param input from which the spaces and the new-line should be deleted.
     */
    public static void eatEscapedNL(Input input) {
        if (input.length() > 0 && input.charAt(0) == '\\') {
            int i = 1;
            while (i < input.length() && Character.isWhitespace(input.charAt(i)) && input.charAt(i) != '\n') {
                i++;
            }
            if (i < input.length() && input.charAt(i) == '\n') {
                skip(input, i + 1);
            }
        }
    }


    /**
     * Delete the characters from the start of the input until after the first EOL
     *
     * @param input from which the spaces should be deleted.
     */
    public static void skip2EOL(Input input) {
        fetch2EOL(input);
    }

    /**
     * Delete the characters from the start of the input until after the first EOL and return the deleted start as a
     * String. The returned string does not contain the closing new line, but the closing new line is removed from the
     * input.
     *
     * @param input from which the spaces should be deleted.
     * @return the string from the original start of the input to the end of line, not including the end of line
     */
    public static String fetch2EOL(Input input) {
        final var sb = new StringBuilder();
        while (input.length() > 0 && input.charAt(0) != '\n') {
            sb.append(input.charAt(0));
            input.delete(1);
        }
        if (input.length() > 0 && input.charAt(0) == '\n') {
            input.delete(1);
        }
        return sb.toString();
    }

    /**
     * Get the parameter list that is at the start of the input. The parameter list has to start with a {@code (}
     * character and should be closed with a {@code )} character. The parameters are separated by {@code ,} characters,
     * and starting and ending spaces from the parameters are removed.
     *
     * <pre>
     *         ( a,b, c ,d)
     * </pre>
     * <p>
     * There is no restriction on what characters the parameter names can contain other than those implied by the
     * parsing algorithm: you cannot use {@code )} and {@code ,} characters in a parameter and you cannot have space at
     * the start and at the end of the parameter. It is recommended not to abuse this possibility.
     *
     * @param input that contains the parameter list
     * @param id    the id of the macro that has this parameter list. This parameter is only used for error reporting.
     * @return the array containing the parameter, or an empty (zero length) array if there are no parameters (when the
     * first character is not {@code (} opening paren)
     * @throws BadSyntaxAt when the input starts with a {@code (} character, therefore it is supposed to have parameters
     *                     but the parameter list if any is not closed with a {@code )} character.
     */
    public static String[] getParameters(Input input, String id) throws BadSyntaxAt {
        final var ref = input.getPosition();
        if (firstCharIs(input, '(')) {
            skip(input, 1);
            var closingParen = input.indexOf(")");
            BadSyntaxAt.when(!contains(closingParen), "'" + id + "' has parameters, but no ')'", ref);
            var param = input.substring(0, closingParen);
            skip(input, closingParen + 1);
            skipWhiteSpaces(input);
            if (param.length() == 0) {
                return new String[0];
            } else {
                return ensure(Arrays.stream(param.split(",")).map(String::trim).toArray(String[]::new), ref);
            }
        } else {
            return new String[0];
        }
    }

    /**
     * Checks that a string is a valid identifier.
     *
     * @param id the string holding the identifier or something else
     * @return true if the string syntactically is a valid identifier
     */
    public static boolean isIdentifier(final String id) {
        final char startCharacter = id.charAt(0);
        if (!validId1stChar(startCharacter)) {
            return false;
        }
        for (int i = 1; i < id.length(); i++) {
            if (!validIdChar(id.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(final String id) {
        if (id.length() == 0) {
            return false;
        }
        for (int i = id.charAt(0) == '-' || id.charAt(0) == '+' ? 1 : 0; i < id.length(); i++) {
            if (!Character.isDigit(id.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that no parameter name contains another parameter name. If there is any parameter name that contains
     * another parameter name then {@code BadSyntax} is thrown.
     * <p>
     * This restriction ensures that the parameter replacement with the actual values is definite and there are no
     * readability issues.
     *
     * @param parameters the parameters to check
     * @param ref        the position in the input
     * @return the parameters themselves
     * @throws BadSyntaxAt is any of the parameter names contain another parameter name.
     */
    public static String[] ensure(String[] parameters, Position ref) throws BadSyntaxAt {
        final var exceptionParameters = new ArrayList<String>();
        for (int i = 0; i < parameters.length; i++) {
            BadSyntaxAt.when(requireNonNull(parameters[i]).length() == 0, "User defined macro argument cannot be empty string.", ref);
            for (int j = 0; j < parameters.length; j++) {
                if (i != j) {
                    if (parameters[i].contains(parameters[j])) {
                        exceptionParameters.add("" + i + ". parameter '" + parameters[i] + "' contains the "
                                + j + ". parameter '" + parameters[j] + "'");
                    }
                }
            }
        }
        if (!exceptionParameters.isEmpty()) {
            final var badSyntax = new BadSyntaxAt("User defined macro parameter name should not be a substring of another parameter.", ref);
            badSyntax.parameters(exceptionParameters);
            throw badSyntax;
        }
        return parameters;
    }

    /**
     * Parse the input and split it up into a String array. It can be used in many macros to provide a consistent syntax
     * and structure when the macro processing needs a list of strings.
     * <p>
     * The possible syntax variations are:
     * <pre>
     * macroName / a / b / c / ... /x
     * macroName   a   b   c   ...  x
     * macroName `regex` separator a separator b separator .... separator x
     * </pre>
     * <p>
     * where the separator character is the first non-whitespace character after the macro name, and it is not the
     * back-tick (`) character. If the first non-whitespace character after the name of the macro id is a backtick then
     * the parsing expects to be a regular expression till the next backtick. After the regular expression and after the
     * closing backtick the rest of the input is spit up and the separator is the regular expression.
     * <p>
     * Backtick was selected during the design of the syntax to enclose the regular expression because this character is
     * very rare in Java regular expression. In case you need one inside the regular expression then you have to simply
     * double it and the parsing will single it back.
     * <p>
     * If the first character after the white spaces is a digit or alpha character then the input will be split along
     * the spaces.
     *
     * @param input to be split up
     * @return the array of the strings created from the input
     */
    public static String[] getParts(Input input) {
        return getParts(input, -1);
    }

    /**
     * Same as {@link #getParts(Input)} but we want at most {@code limit} number of parts.
     *
     * @param input the input from which we want to get the parts
     * @param limit the maximum number of parts we need
     * @return the parts of the input in an array
     */
    public static String[] getParts(Input input, int limit) {
        skipWhiteSpaces(input);
        if (input.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        final var separator = input.substring(0, 1);
        if (Character.isLetterOrDigit(separator.charAt(0))) {
            return input.toString().split("\\s+", limit);
        }
        skip(input, 1);
        if ("`".equals(separator)) {
            return getPartsRegex(input, limit);
        }
        return input.toString().split(Pattern.quote(separator), limit);
    }

    private static String[] getPartsRegex(Input input, int limit) {
        final var regex = fetchRegex(input);
        return skipEmptyFirst(input.toString().split(regex, limit));
    }

    private static String[] skipEmptyFirst(String[] values) {
        if (values.length > 0 && values[0].length() == 0) {
            return Arrays.copyOfRange(values, 1, values.length);
        } else {
            return values;
        }
    }

    /**
     * Fetch a regular expression from the input. The regular expression starts at the start of the input, and it is
     * terminated by a {@code `} (backtick) character or by the end of the input. If there are backtick characters in
     * the regular expression they have to be doubled. After the fetching the first character on the input is the
     * character that follows the terminating backtick character.
     *
     * @param input that starts with the regular expression. It is also modified so that the regular expression at the
     *              start and the terminating back-tick is removed.
     * @return the regular expression. The return string may contain single back-ticks, which are doubled in the input.
     */
    private static String fetchRegex(Input input) {
        var sb = new StringBuilder();
        while (input.length() > 0) {
            while (input.charAt(0) == '`' && input.length() > 1 && input.charAt(1) == '`') {
                sb.append('`');
                skip(input, 2);
            }
            if (input.charAt(0) == '`') break;
            sb.append(input.charAt(0));
            skip(input, 1);
        }
        if (input.length() > 0) {
            skip(input, 1);
        }
        return sb.toString();
    }

}
