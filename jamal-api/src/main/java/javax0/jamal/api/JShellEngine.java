package javax0.jamal.api;

/**
 * Simple interface that defines the two methods that a JShell embedding engine has to implement.
 */
public interface JShellEngine extends AutoCloseable {
    /**
     * Evaluate the input string using the JShell interpreter.
     * <p>
     * The output is usually the string that the snippet or snippets print to the {@code System.out} using {@code
     * System.out.print()} or {@code System.out.println()} or some other way. If this string has zero length then the
     * evaluation should return the value of the last evaluated snippet.
     * <p>
     * If the JShell throws an exception or the evaluation status is some error then the method throws {@code
     * BadSyntax}.
     *
     * @param input text to evaluate as JShell snippet
     * @return the standard output of the snippet
     * @throws BadSyntax if there was any error during the evaulation of the input or during the evaluation of the
     *                   deferred definitions.
     */
    String evaluate(String input) throws BadSyntax;

    /**
     * Evaluate the input assuming that this code defines something for the JShell interpreter, like a method, class
     * variable etc.
     * <p>
     * The implementation has to be ready to defer the evaluation of the call. If there is no call to {@link
     * #evaluate(String)} then there is no reason to execute the actual definition creating a JShell process. The
     * implementation should care not to spawn a JShell process, only when it is needed.
     *
     * @param input the input that defines one single something, like a class, method, variable. It should not include
     *              multiple definitions.
     * @throws BadSyntax when the evaluation results an exception or rejects the snippet
     */
    void define(String input) throws BadSyntax;
}
