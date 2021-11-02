package javax0.jamal.api;

/**
 * Collection of character constants that have special meaning in Jamal.
 */
public class SpecialCharacters {
    /**
     * When this character is used in front of a built-in macro, the input of the macro is evaluated before the
     * invoking {@link Macro#evaluate(Input, Processor)}.
     */
    public static final char PRE_EVALUATE = '#';
    /**
     * The opposite of {@link #PRE_EVALUATE}. When this character is used in front of a built-in macro, the input of the
     * macro is NOT evaluated before the invoking {@link Macro#evaluate(Input, Processor)}.
     */
    public static final char NO_PRE_EVALUATE = '@';
    /**
     * When this character is used before the identifier of a macro (user defined or built-in) the result will be
     * evaluated. If the macro is a user-defined and non-verbatim macro then this will already be the second evaluation.
     * There can be several {@link #POST_VALUATE} character in front of a macro and that many evaluations will happen.
     */
    public static final char POST_VALUATE = '!';
    /**
     * When this character is used before the identifier of a macro (user defined or built-in) the macro itself will
     * not be evaluated. The result will be the opening string, the closing string, all the character between except
     * that the {@link #IDENT} character is removed. The name {@code IDENT} comes from the similarity of the
     * {@code ident} macro defined in the core package.
     */
    public static final char IDENT = '`';
    /**
     * The word "shebang" is used to denote the character sequence at the start of the UNIX script files that the shell
     * uses to identify how to start the script. The characters {@link #IMPORT_SHEBANG1} and {@link #IMPORT_SHEBANG2}
     * may inform Jamal about how to import a file.
     * <p>
     * The macro opening and closing strings are not hard-wired. In {@code pom.jam} files it is usually opening and
     * closing curly braces. In documentation sources this is many times curly brace open, plus {@code %} to opening
     * and {@code %} and curly brace close for closing.
     * <p>
     * When a Jamal source file imports a file it is assumed that the opening and the closing string is the same in the
     * imported file as in the importing one. However, it is not always the case. There may be some general purpose
     * import files that define macros usable in multiple environments. In that case the importing file has to change
     * the opening and closing string using the {@code sep} macro, import a file and changing back the strings to the
     * original. This is simplified using the import shebangs.
     * <p>
     * When a file imported or included starts with the {@link #IMPORT_SHEBANG1} and {@link #IMPORT_SHEBANG2} as the
     * very first two characters then Jamal will assume that the opening and the closing strings are the
     * {@link #IMPORT_OPEN} and {@link #IMPORT_CLOSE} strings and it will automatically switch to those and back after
     * the import or include has finished.
     */
    public static final char IMPORT_SHEBANG1 = '{';
    /**
     * See {@link #IMPORT_SHEBANG1}
     */
    public static final char IMPORT_SHEBANG2 = '@';
    /**
     * See {@link #IMPORT_SHEBANG1}
     */
    public static final String IMPORT_OPEN = "{";
    /**
     * See {@link #IMPORT_SHEBANG1}
     */
    public static final String IMPORT_CLOSE = "}";
    /**
     * When this character is used with some built-in macros then the behaviour will query.
     */
    public static final char QUERY = '?';
    /**
     * When this character is used with some built-in macros then the behaviour will force an error.
     */
    public static final char REPORT_ERRMES = '!';
    /**
     * When a user defined macro is used the result of the macro is the content of the macro with the parameters
     * replaced with the actual values. This result is then processed for further macros, unless the user defined
     * macro is verbatim. This character is used following the {@code define} keyword to signal that the user defined
     * macro is a verbatim macro.
     */
    public static final char DEFINE_VERBATIM = '~';
    /**
     * This character can be used in a {@code define} macro to signal that the named macro should only be defined if
     * it was not defined yet, otherwise just keep the original. Without this character the macro would be redefined.
     */
    public static final char DEFINE_OPTIONALLY = '?';
    /**
     * This character can be used in a {@code define} macro to signal that the named macro should be defined if
     * it was ot defined yet, otherwise and error would happen. Without this character the macro would be redefined.
     */
    public static final char ERROR_REDEFINE = '!';
    /**
     * Use this character in front of the use of the user defined macro. If the macro is not defined the result is
     * an empty string. Without this character the use of an undefined user defined macro causes an error.
     */
    public static final char REPORT_UNDEFINED = '?';
}
