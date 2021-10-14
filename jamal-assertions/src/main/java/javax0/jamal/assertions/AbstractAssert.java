package javax0.jamal.assertions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;

/**
 * Abstract class implementing the common parts of all concrete assertions.
 * <p>
 * All assertions work on a set of parameters and options.
 * The options are parsed using the {@link Params} class.
 * The parameters are parsed using the {@link InputHandler#getParts(Input)} parsing.
 * <p>
 * There are two options implemented in this class:
 * <ul>
 * <li>{@code trim} (alias {@code strip}), and</li>
 * <li>{@code not} (alias {@code negate})</li>
 * </ul>
 * The option {@code trim} will trim the white space from around the parameters before performing the assertion {@link #test(String[])}.
 * The option {@code not} will invert the result of the test.
 */
abstract class AbstractAssert implements Macro {

    private final int N;
    private final String defaultMessage;
    private final String negatedDefaultMessage;

    /**
     * This constructor must be invoked by the constructor of the child class.
     *
     * @param N                     the maximum number of parameters the assertion needs.
     *                              Assertions can work with N or with N-1 parameters.
     *                              The last parameter is the error message, and it is optional.
     * @param defaultMessage        the default message in case the message parameter is missing and there is no negation in the options.
     * @param negatedDefaultMessage the default message in case the message parameter is missing and there <i>is</i> negation in the options.
     */
    protected AbstractAssert(int N, String defaultMessage, String negatedDefaultMessage) {
        this.N = N;
        this.defaultMessage = defaultMessage;
        this.negatedDefaultMessage = negatedDefaultMessage;
    }

    /**
     * Child classes have to implement this method.
     *
     * @param parts the parts that have at least {@code N-1} parts.
     *              If there were not enough parts an exception was already thrown and this method is not invoked.
     * @return {@code true} if the assertion is okay and {@code false} otherwise.
     * This method should not care about trimming or negation, it is done before and after the method was executed.
     */
    protected abstract boolean test(String[] parts) throws BadSyntax;

    static boolean negateIfNeeded(boolean b, Params.Param<Boolean> not) throws BadSyntax {
        return b == !not.is();
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        // snippet Assertion_params
        Params.Param<Boolean> trim = Params.<Boolean>holder("trim", "strip").asBoolean();
        Params.Param<Boolean> not = Params.<Boolean>holder("not", "negate").asBoolean();
        //end snippet

        Params.using(processor).between("()").from(this).keys(trim, not).parse(input);
        String[] parts = getParts(input, N, trim, this);
        if (negateIfNeeded(test(parts), not)) {
            return "";
        }
        final String format = not.is() ? negatedDefaultMessage : defaultMessage;
        if (parts.length >= N && parts[N - 1].trim().length() > 0 ) {
            throw new BadSyntax(String.format(parts[N - 1], (Object[]) parts));
        }
        throw new BadSyntax(getId() + " has failed " + String.format(format, (Object[]) parts));
    }

    /**
     * The implementation of the naming assumes that the name of the class looks like {@code AsserXXX} where
     * {@code Assert} is a six character prefix, usually it is literally {@code 'Assert'} and it is followed by
     * the name of the assertion. The name of the macro will be {@code assert:} followed by the rest of the name of
     * the class lowercasing the first characters. For example:
     * <pre>{@code
     * AssertEquals -> assert:equals
     * AssertSomeFunnyName -> assert:somFunnyName
     * AssireAny -> assert:any
     * }</pre>
     *
     * If the naming of the class does not conform to this schema or the macro implemented needs some special name then
     * the method has to be overridden.
     *
     * @return the name of the macro
     */
    @Override
    public String getId() {
        final var s = this.getClass().getSimpleName().substring(6);
        return "assert:" + s.substring(0,1).toLowerCase() + s.substring(1);
    }

    private static String[] getParts(Input input, int N, Params.Param<Boolean> trim, Macro macro) throws BadSyntax {
        final var parts = InputHandler.getParts(input, N);
        if (parts.length < N - 1) {
            throw new BadSyntax(macro.getId() + " needs at least " + (N - 1) + " arguments");
        }
        if (trim.is()) {
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].strip();
            }
        }
        return parts;
    }
}
