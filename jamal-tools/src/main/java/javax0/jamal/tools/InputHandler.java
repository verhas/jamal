package javax0.jamal.tools;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Utility class with some simple static methods that fetch characters from an input buffer.
 */
public class InputHandler {
    final static private int DOES_NOT_CONTAIN = -1;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * @param s a character sequence of which the first character is checked
     * @param c the character we are looking for
     * @return returns true if the first character of 's' is 'c'
     */
    public static boolean firstCharIs(CharSequence s, char c) {
        return s.length() > 0 && s.charAt(0) == c;
    }

    /**
     * Delete the start of the input
     *
     * @param input              from which the first characters are deleted
     * @param numberOfCharacters the number of characters to be deleted from the start of {@code input}
     */
    public static void skip(Input input, int numberOfCharacters) {
        input.delete(numberOfCharacters);
    }

    /**
     * Delete the start of the input
     *
     * @param input from which the first characters are deleted
     * @param s     is a string that is supposed to be on the start of the input and this string is going to
     *              be deleted from the start of the {@code input}. The actual implementation does not check that the
     *              string is really there at the start of the input, it just skips so many characters as many
     *              the string has.
     */
    public static void skip(Input input, String s) {
        skip(input, s.length());
    }

    /**
     * @param i the result of {@link String#indexOf(int)}
     * @return {@code true} the value is a valid character code and not a signal that the string does not contain
     * the character we are looking for.
     */
    public static boolean contains(int i) {
        return i != DOES_NOT_CONTAIN;
    }

    /**
     * Fetch an id from the start of the {@code input}.
     * <p>
     * An identifier is a string that starts with a character accepted by {@link #validId1stChar(char)} and contain
     * only characters that are accepted {@link #validIdChar(char)}
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
     * which is an experimental feature and is deliberately not documented except here.
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

    public static boolean isGlobalMacro(String id) {
        return id.contains(":");
    }

    /**
     * Convert a global macro name.
     * <p>
     * Macro names that contain the '{@code :}' character are global macros and automatically are defined on the
     * top level. This provides a way to macro package developers to use name spacing, although Jamal does not
     * handle name spaces, the names can be treated as 'namespace:localName' or even name space notations can be
     * nested.
     * <p>
     * A global macro without name space starts with a '{@code :}' character when defined but this character is
     * removed by this conversion so later the macro can be referred to with the name without the {@code :} character.
     *
     * @param id the identifier of the macro.
     * @return the converted identifier.
     */
    public static String convertGlobal(String id) {
        if (id.length() > 0 && id.charAt(0) == ':') {
            return id.substring(1);
        } else {
            return id;
        }
    }

    /**
     * @param c the character to check
     * @return {@code true} if the character can be used as the first character of a macro identifier. Currently
     * these are {@code $}, {@code _} (underscore), {@code :} (colon) and any alphabetic character.
     */
    public static boolean validId1stChar(char c) {
        return c == '$' || c == '_' || c == ':' || Character.isAlphabetic(c);
    }

    /**
     * @param c the character to check
     * @return {@code true} if the character can be used in a macro identifier. These are the same characters that
     * can be used as first characters (see {@link #validId1stChar(char)}) and also digits.
     */
    public static boolean validIdChar(char c) {
        return validId1stChar(c) || Character.isDigit(c);
    }

    /**
     * Delete the white space characters from the start of the input
     *
     * @param input from which the spaces should be deleted.
     */
    public static void skipWhiteSpaces(Input input) {
        while (input.length() > 0 && Character.isWhitespace(input.charAt(0))) {
            input.delete(1);
        }
    }

    /**
     * Delete the white space characters from the start of the input but only until after the first EOL
     *
     * @param input from which the spaces should be deleted.
     */
    public static void skipWhiteSpaces2EOL(Input input) {
        while (input.length() > 0 && Character.isWhitespace(input.charAt(0)) && input.charAt(0) != '\n') {
            input.delete(1);
        }
        if( input.length() > 0 && input.charAt(0) == '\n' ){
            input.delete(1);
        }
    }

    /**
     * Copy the string from the start of the input to the end of the output.
     *
     * @param input  from which the string will be removed
     * @param output to which the string will be appended
     * @param s      the string. There is no check that the input really starts with the string. The string is
     *               {@link #skip(Input, String)}-ped in the {@code input} and is appended to the output.
     */
    public static void copy(Input input, Input output, String s) {
        skip(input, s);
        output.append(s);
    }

    /**
     * Get the parameter list that is at the start of the input. The parameter list has to start with a
     * {@code (} character and should be closed with a {@code )} character. The parameters are separated
     * by {@code ,} characters, and starting and ending spaces from the parameters are removed.
     *
     * <pre>
     *         ( a,b, c ,d)
     * </pre>
     *
     * There is no restriction on what characters the parameter names can contain other than those implied by the
     * parsing algorithm: you cannot use {@code )} and {@code ,} characters in a parameter and you cannot have
     * space at the start and at the end of the parameter. It is recommended not to abuse this possibility.
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
        final String[] params;
        if (firstCharIs(input, '(')) {
            skip(input, 1);
            var closingParen = input.indexOf(")");
            if (!contains(closingParen)) {
                throw new BadSyntaxAt("'" + id + "' has parameters, but no ')'", ref);
            }
            var param = input.substring(0, closingParen);
            skip(input, closingParen + 1);
            skipWhiteSpaces(input);
            if (param.length() == 0) {
                return new String[0];
            } else {
                return Arrays.stream(param.split(",")).map(String::trim).toArray(String[]::new);
            }
        } else {
            return new String[0];
        }
    }

    /**
     * Parse the input and split it up into a String array. It can be
     * used in many macros to provide a consistent syntax and structure
     * when the macro processing needs a list of strings.
     * <p>
     * The possible syntax variations are:
     * <pre>
     * macroName / a / b / c / ... /x
     * </pre>
     * <p>
     * where the separator character is the first non-whitespace
     * character after the macro name, and it is not the back-tick (`)
     * character. If the first non-whitespace character after the name
     * of the macro id is a backtick then the parsing expects to be a
     * regular expression till the next backtick. After the regular
     * expression and after the closing backtick the rest of the input
     * is spit up and the separator is the regular expression.
     * <p>
     * Backtick was selected during the design of the syntax to enclose
     * the regular expression because this character is very rare in
     * Java regular expression. In case you need one inside the regular
     * expression then you have to simply double it and the parsing will
     * single it back.
     *
     * <pre>
     * macroName `regex` separator a separator b separator .... separator x
     * </pre>
     *
     * @param input to be split up
     * @return the list of the strings created from the input
     * @throws BadSyntaxAt if the separator character is letter or digit
     */
    public static String[] getParts(Input input) throws BadSyntaxAt {
        skipWhiteSpaces(input);
        if (input.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        var sepchar = input.substring(0, 1);
        if(Character.isLetterOrDigit(sepchar.charAt(0))){
            throw new BadSyntaxAt("The separator character '"+sepchar+"' is invalid",input.getPosition());
        }
        skip(input, 1);
        if ("`".equals(sepchar)) {
            return getPartsRegex(input);
        }
        return input.toString().split(Pattern.quote(sepchar), -1);
    }

    private static String[] getPartsRegex(Input input) {
        final var regex = fetchRegex(input);
        return input.toString().split(regex, -1);
    }

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
