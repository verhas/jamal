package javax0.jamal.api;

/**
 * Something that can be evaluated. The evaluation may use strings as arguments. Typically user defined macros and
 * scripts are evaluable.
 */
public interface Evaluable {

    /**
     * Evaluate the user defined macro and return the result.
     *
     * @param parameters the parameter values
     * @return the evaluated result string
     * @throws BadSyntax if there is some error during the evaluation of the macro
     */
    String evaluate(String... parameters) throws BadSyntax;

    /**
     * @return the number of parameters that the user defined macro expects or -1 if the macro can accept any number of
     * parameters. This may be the case when an extension defines a special user defined macro, like the regular
     * expression module. The result currently is used by the processor to handle the parameters of the user defined
     * macros differently that accept one single parameter.
     */
    int expectedNumberOfArguments();
}
