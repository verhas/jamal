package javax0.jamal.api;

/**
 * When a macro implements this interface then it has the freedom to fetch the body of the macro from the input. Macros
 * start with the macro start string and end with the macro end string and the nested macros should pair up with their
 * macro opening and closing strings.
 * <p>
 * When a macro implements this interface one of these constrains is waived. Macros still should start and stop with the
 * macro opening and closing string, but an escape macro can decide itself how it interprets the macros on the input
 * inside.
 * <p>
 * The macro {@code escape} in the core module, for example demands that the input starts with a back-tick enclosed
 * string and the end of the macro, before the macro closing string is also has the same string. At the start it finds
 * the string and then it looks for the same string again on the input and it does not care if there are any other
 * macros with opening and closing strings nested properly or unmatched between.
 * <p>
 * The actual process in Jamal checks that the macro implements this interface when the macro is found inside the input
 * and then instead of the built-in parsing the parsing invokes the {@link #moveBody(Processor, Input, Input)
 * moveBody()} method.
 * <p>
 * For further information and to see an example implementation have a look at the {@code escape} macro in the core
 * module.
 */
public interface Escape {
    /**
     * Move the body of the macro from the input to the output.
     * <p>
     *
     * @param processor
     * @param input     is the input positioned after the macro opening string and after the optional post evaluate and
     *                  ident protection characters: {@code !} and {@code `}. There may be white spaces before the @ or
     *                  {@code #} character.
     * @param output    where the body of the macro has to be moved
     * @throws BadSyntaxAt if the input does not satisfy then special escaped requirements.
     */
    void moveBody(Processor processor, Input input, Input output) throws BadSyntaxAt;
}
