package javax0.jamal.api;

/**
 * Something that can be evaluated. The evaluation may use strings as arguments. Typically, user defined macros and
 * scripts are evaluable.
 */
public interface Evaluable extends Identified {

    /**
     * Evaluate the user defined macro and return the result.
     *
     * @param parameters the parameter values
     * @return the evaluated result string
     * @throws BadSyntax if there is some error during the evaluation of the macro
     */
    String evaluate(String... parameters) throws BadSyntax;

    /**
     * The output of this evaluable should be treated verbatim. When this method return true, then evaluating
     * <pre>{@code
     *    {userDefinedMacro}
     * }</pre>
     * <p>
     * will have the same result as
     *
     * <pre>{@code
     *    {@verbatim userDefinedMacro}
     * }</pre>
     *
     * @return {@code true} if the result of the macro should not be further evaluated
     */
    default boolean isVerbatim() {
        return false;
    }

    /**
     * @return the number of parameters that the user defined macro expects or -1 if the macro can accept any number of
     * parameters. This may be the case when an extension defines a special user defined macro, like the regular
     * expression module. The result currently is used by the processor to handle the parameters of the user defined
     * macros differently that accept one single parameter.
     * <p>
     * Also, when a macro is named {@code default} and has arguments and the first argument is named {@code $macro} or
     * {@code $_} then the evaluation gives the name of the macro as an extra into this argument and all other arguments
     * get filled with the strings that comes from the macro input. This first argument is interesting, when the user
     * defined macro {@code default} is invoked because the macro used was not defined.
     */
    int expectedNumberOfArguments();

    /**
     * Set the identifier that was used to identify the macro. It may not be the same as the one the macro thinks about
     * itself. The built-in {@code javax0.jamal.engine.UserDefinedMacro} does not use this feature, but other
     * implementations can rely on the actual id, when the same {@link Evaluable} instance is registered with different
     * names, or when the user defined macro {@code default} is defined by some Java code and not the built-in
     * {@code UserDefinedMacro}.
     * <p>
     * This method is invoked by the processor right before invoking evaluate. It is important to note that processor
     * instances are single threaded.
     *
     * @param id the actual identifier that was used in the source
     */
    default void setCurrentId(String id) {
    }

}
