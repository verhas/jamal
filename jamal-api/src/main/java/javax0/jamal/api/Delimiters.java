package javax0.jamal.api;

/**
 * Simple macro delimiter string storage interface.
 * <p>
 * The implementing classes store the actual macro open and macro close strings and provide means to
 * alter the actual values.
 */
public interface Delimiters {
    /**
     * @return the current macro opening string.
     */
    String open();

    /**
     * @return the current macro closing string
     */
    String close();

    /**
     * Sets the opening and closing delimiter strings, or implementation may reset some old value if the arguments
     * are {@code null}
     *
     * @param openDelimiter  the macro opening string to be set. If this parameter is {@code null} then
     *                       the implementation may treat this information as a restore process. For example
     *                       the class {@link MacroRegister} saves the old values of the separators in a stack
     *                       and when {@code openDelimiter} is {@code null} it restores the delimiters from the
     *                       top of the stack.
     * @param closeDelimiter the macro closing string to be set
     * @throws BadSyntax in case the separators can not be set.
     */
    void separators(String openDelimiter, String closeDelimiter) throws BadSyntax;
}
