package javax0.jamal.assertions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;

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
abstract class AbstractAssert implements Macro, Scanner {

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

    static boolean negateIfNeeded(boolean b, BooleanParameter not) throws BadSyntax {
        return b == !not.is();
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var scanner = newScanner(input, processor);
        // snippet Assertion_params
        final var trim = scanner.bool("trim", "strip");
        final var not = scanner.bool("not", "negate");
        final var test = scanner.bool("test", "boolean", "bool");
        //end snippet
        scanner.done();

        String[] parts = getParts(input, processor, N, trim, this);
        if (negateIfNeeded(test(parts), not)) {
            return test.is() ? "true" : "";
        } else {
            if (test.is()) {
                return "false";
            } else {
                final String format = not.is() ? negatedDefaultMessage : defaultMessage;
                if (parts.length >= N && !parts[N - 1].trim().isEmpty()) {
                    throw new BadSyntax(String.format(parts[N - 1], (Object[]) parts));
                }
                throw new BadSyntax(getId() + " has failed " + String.format(format, (Object[]) parts));
            }
        }
    }

    private static String[] getParts(Input input, Processor processor, int N, BooleanParameter trim, Macro macro) throws BadSyntax {
        final var parts = InputHandler.getParts(input, processor, N);
        BadSyntax.when(parts.length < N - 1, () -> macro.getId() + " needs at least " + (N - 1) + " arguments");
        if (trim.is()) {
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].strip();
            }
        }
        return parts;
    }
}
