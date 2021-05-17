package javax0.jamal.api;

import java.util.List;
import java.util.Optional;

import static javax0.jamal.api.SpecialCharacters.IDENT;
import static javax0.jamal.api.SpecialCharacters.NO_PRE_EVALUATE;
import static javax0.jamal.api.SpecialCharacters.POST_VALUATE;
import static javax0.jamal.api.SpecialCharacters.PRE_EVALUATE;

/**
 * Any class that wants to function as a macro implementation should implement this interface.
 * <p>
 * The built-in macro {@code use} implemented in {@code javax0.jamal.builtins.Use} in the core package also assumes that
 * the class has a zero-parameter (default) constructor.
 * <p>
 * Macro implementations are supposed to be state-less, but they can have state. Be careful, however, that the macros
 * can have many instances while processing a single file if they come into life via the {@code use} macro.
 */
@FunctionalInterface
public interface Macro extends Identified, ServiceLoaded {
    static List<Macro> getInstances() {
        return ServiceLoaded.getInstances(Macro.class);
    }

    Macro FETCH = (Input in, Processor processor) -> null;

    /**
     * @param c the character to check
     * @return {@code true} if the character can be used as the first character of a macro identifier. Currently these
     * are {@code $}, {@code _} (underscore), {@code :} (colon) and any alphabetic character.
     */
    static boolean validId1stChar(char c) {
        return c == '$' || c == '_' || c == ':' || Character.isAlphabetic(c);
    }

    /**
     * @param c the character to check
     * @return {@code true} if the character can be used in a macro identifier. These are the same characters that can
     * be used as first characters (see {@link #validId1stChar(char)}) and also digits.
     */
    static boolean validIdChar(char c) {
        return validId1stChar(c) || Character.isDigit(c);
    }

    /**
     * This method reads the input an returns the result of the macro as a String.
     * <p>
     * When the macro is used, like
     * <pre>{@code
     *       {@builtInMacro this is the input}
     * }</pre>
     * <p>
     * then the input will contain '{@code this is the input}' without the spaces that are between the macro name and
     * the first non-space character, which is the word '{@code this}' as in the example.
     *
     * @param in        the input that is the "parameter" to the built-in macro
     * @param processor the processor that executes the macro. See {@link Processor}
     * @return the result string that will be inserted into the output in the place of the macro use
     * @throws BadSyntax the evaluation should throw this exception with reasonable message text in case the input has
     *                   bad format.
     */
    String evaluate(Input in, Processor processor) throws BadSyntax;

    /**
     * When a built-in macro is registered then the name used in the source file will be the string returned by this
     * method. When a macro is registered using the built-in macro {@code use} (see {@code javax0.jamal.builtins.Use})
     * the the caller can provide an alias. Even when the proposed use is to be declared through the {@code use} macro
     * it is recommended to provide a reasonable id.
     *
     * @return the id/name of the macro
     */
    // snippet getId
    default String getId() {
        return this.getClass().getSimpleName().toLowerCase();
    }
    // end snippet


    /**
     * Fetch the body of the macro including the closing string.
     * <p>
     * This method is invoked when the macro body is read from the input before processing and the macro body is inside
     * another macro. For example we have the macro structure:
     *
     * <pre>{@code
     *
     *  {%@comment {%@define a=1%}%}
     *
     * }</pre>
     * <p>
     * The processing will call {@link #fetch(Processor, Input) fetch()} for the macro {@code comment} and that will
     * return
     * <pre>{@code
     * @comment {%@define a=1%}
     * }</pre>
     * <p>
     * During this the recursive call will call {@code prefecth()} when processing the macro {@code define}. This method
     * will return
     *
     * <pre>{@code
     * @define a=1%}
     * }</pre>
     * <p>
     * The return value of this method is used by the processor to build up the higher level, embedding macros that will
     * then be evaluated. On the other hand the macro {@link #fetch(Processor, Input) fetch()} is invoked when the macro
     * itself is evaluated.
     * <p>
     * The default implementation of this method invoked {@link #fetch(Processor, Input) fetch()} and then appends the
     * current macro closing string.
     *
     * @param processor the processor that executes the macro. See {@link Processor}
     * @param input     the input that is the "parameter" to the built-in macro. The position of the input is right
     *                  after the macro opening string. The method copies this to the return value including the macro
     *                  closing string.
     * @return the macro body including the closing string (but not the opening string)
     * @throws BadSyntaxAt
     */
    default String prefetch(Processor processor, Input input) throws BadSyntaxAt {
        return fetch(processor, input) + processor.getRegister().close();
    }

    /**
     * Same as {@link #prefetch(Processor, Input) prefetch()} but the returned string does not contain the closing
     * string.
     *
     * @param processor the processor that executes the macro. See {@link Processor}
     * @param input     the input that is the "parameter" to the built-in macro. The position of the input is right
     *                  after the macro opening string. The method moved this to the return value. The result does not
     *                  contain the closing macro closing string, but the it is removed from the input.
     * @return the macro body without the opening or closing string
     * @throws BadSyntaxAt
     */
    default String fetch(Processor processor, Input input) throws BadSyntaxAt {
        final var output = new StringBuilder();
        final var open = processor.getRegister().open();
        final var close = processor.getRegister().close();
        var op = new PositionStack(input.getPosition());
        while (op.size() > 0) {// while there is any opened macro
            if (input.length() == 0) {
                throw new BadSyntaxAt("Macro was not terminated in the file.", op.pop());
            }

            if (input.indexOf(open) == 0) {
                int offset = open.length();
                while (offset < input.length() && input.charAt(offset) == IDENT
                    || input.charAt(offset) == POST_VALUATE
                    || Character.isWhitespace(input.charAt(offset))) {
                    offset++;
                }
                final var macro = getMacro(processor.getRegister(), input, offset);
                if (macro.isPresent()) {
                    output.append(input.substring(0, offset));
                    input.delete(offset);
                    output.append(macro.get().prefetch(processor, input));
                } else {
                    output.append(input.substring(0, open.length()));
                    input.delete(open.length());
                    op.push(input.getPosition());
                }
            } else if (input.indexOf(close) == 0) {
                if (op.popAndEmpty()) {
                    input.delete(close.length());
                    if (input.length() > 0 && input.charAt(0) == '\\') {
                        int i = 1;
                        while (i < input.length() && Character.isWhitespace(input.charAt(i)) && input.charAt(i) != '\n') {
                            i++;
                        }
                        if (i < input.length() && input.charAt(i) == '\n') {
                            input.delete(i + 1);
                        }
                    }
                } else {
                    output.append(input.substring(0, close.length()));
                    input.delete(close.length());
                }
            } else {
                final var oIndex = input.indexOf(open);
                final var cIndex = input.indexOf(close);
                final int textEnd;
                if (cIndex != -1 && (oIndex == -1 || cIndex < oIndex)) {
                    textEnd = cIndex;
                } else if (oIndex > -1) {
                    textEnd = oIndex;
                } else {
                    textEnd = input.length();
                }
                output.append(input.substring(0, textEnd));
                input.delete(textEnd);
            }
        }
        return output.toString();
    }

    static Optional<Macro> getMacro(MacroRegister register, Input input, int offset) {
        while (offset < input.length() && Character.isWhitespace(input.charAt(offset))) {
            offset++;
        }
        if (offset < input.length() && (input.charAt(offset) == NO_PRE_EVALUATE || input.charAt(offset) == PRE_EVALUATE)) {
            offset++;
            while (offset < input.length() && Character.isWhitespace(input.charAt(offset))) {
                offset++;
            }
            int i = offset;
            if (input.length() > 0 && validId1stChar(input.charAt(offset))) {
                while (input.length() > i && validIdChar(input.charAt(i))) {
                    i++;
                }
            } else {
                while (input.length() > i && !Character.isWhitespace(input.charAt(i))) {
                    i++;
                }
            }
            return register.getMacro(input.substring(offset,i));
        } else {
            return Optional.empty();
        }
    }
}
