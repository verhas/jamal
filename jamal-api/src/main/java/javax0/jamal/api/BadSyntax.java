package javax0.jamal.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * This exception, as the name suggests, is thrown when the processor or a macro finds something it cannot interpret.
 * This exception is always captured inside Jamal and then rethrown as a {@link BadSyntaxAt} exception adding the line
 * reference information. This exception is used at code locations where it is not possible to identify the actual input
 * location where the erroneous syntax started. See also {@link BadSyntaxAt}.
 */
public class BadSyntax extends Exception {
    public BadSyntax() {

    }

    public BadSyntax(String message) {
        super(message);
    }

    public BadSyntax(String message, Throwable cause) {
        super(message, cause);
    }

    public BadSyntax(String message, List<Throwable> suppressed) {
        super(message);
        for (final var s : suppressed) {
            this.addSuppressed(s);
        }
    }

    /**
     * Create a runtime exception from a {@code BadSyntax} exception. This is used when the exception is thrown from a
     * method that is not declared to throw {@code BadSyntax} exception, or in a place where the exception is not
     * expected.
     *
     * @param message the message of the exception to the {@code BadSyntax} exception
     * @return the runtime exception
     */
    public static RuntimeException rt(final String message) {
        return new RuntimeException(new BadSyntax(message));
    }

    /**
     * Create a runtime exception from a {@code BadSyntax} exception. This is used when the exception is thrown from a
     * method that is not declared to throw {@code BadSyntax} exception, or in a place where the exception is not
     * expected. The cause of the exception is also provided.
     *
     * @param message the message of the exception to the {@code BadSyntax} exception
     * @param cause   the cause of the exception
     * @return the runtime exception
     */
    public static RuntimeException rt(final String message, Throwable cause) {
        return new RuntimeException(new BadSyntax(message, cause));
    }

    final private List<String> parameters = new ArrayList<>();

    /**
     * Get the parameters of the exception. The parameters are the strings that are added to the exception to provide
     * more information about the error.
     *
     * @return the list of parameters
     */
    public List<String> getParameters() {
        return parameters;
    }

    public BadSyntax parameter(String param) {
        parameters.add(param);
        return this;
    }

    /**
     * Add a list of parameters to the exception. The parameters are the strings that are added to the exception to
     * provide more information about the error.
     *
     * @param param the list of parameters to add
     * @return the exception itself
     */
    public BadSyntax parameters(List<String> param) {
        parameters.addAll(param);
        return this;
    }

    /**
     * Abbreviate the long parameter to a shorter one. If the parameter is longer than 60 characters then it is
     * abbreviated to 60 characters and three dots are added to the end.
     *
     * @param longParam the long parameter to be shortened
     * @return the shortened parameter
     */
    private static String abbreviate(String longParam) {
        if (longParam.length() > 60) {
            return longParam.substring(0, 60) + "...";
        }
        return longParam;
    }

    /**
     * Get the message of the exception. If there are no parameters, then the message is the same as the message of the
     * exception itself.
     *
     * @return the message of the exception
     */
    public String getShortMessage() {
        return super.getMessage();
    }

    /**
     * Get the message of the exception. If there are parameters, then the message is the message of the exception
     * followed by the parameters. The parameters are abbreviated to 60 characters.
     * <p>
     * The parameters are listed in separate lines following the message of the exception, each parameter in its own line.
     *
     * @return the message of the exception
     */
    @Override
    public String getMessage() {
        if (parameters.isEmpty()) {
            return super.getMessage();
        } else {
            return super.getMessage() + "\n" +
                    parameters.stream()
                            .map(BadSyntax::abbreviate)
                            .map(m -> ">>>" + m + "\n")
                            .collect(joining());
        }
    }

    /**
     * A simple supplier where the "get()" can throw {@code BadSyntax}.
     *
     * @param <T>
     */
    public interface ThrowingSupplier<T> {
        T get() throws BadSyntax;
    }

    /**
     * This method throws a {@code BadSyntax} exception when the {@code condition} is {@code true}.
     * The message of the exception is provided by a supplier.
     * <p>
     * There are two versions of the static method {@code when()}. Use this version when the calculation of the
     * exception message is expensive.
     *
     * @param condition when this parameter is {@code true} the method throws {@code BadSyntax} exception.
     *                  when the parameter is {@code false} the method returns.
     * @param message   is the supplier that provides the message for the exception.
     * @throws BadSyntax when the {@code condition} is {@code true}.
     */
    public static void when(final boolean condition, final ThrowingSupplier<String> message) throws BadSyntax {
        if (condition) {
            throw castrated(new BadSyntax(message.get()));
        }
    }

    /**
     * This method throws a {@code BadSyntax} exception when the {@code condition} is {@code true}.
     * The message of the exception is created using the {@code format} and the {@code parameters}.
     * <p>
     * The message formatting happens only when the {@code condition} is {@code true}. The arguments are, however,
     * evaluated before the call.
     *
     * @param condition  when this parameter is {@code true} the method throws {@code BadSyntax} exception.
     *                   when the parameter is {@code false} the method returns.
     * @param format     is message format
     * @param parameters the parameter for the message format
     * @throws BadSyntax when the {@code condition} is {@code true}.
     */
    public static void when(final boolean condition, final String format, final Object... parameters) throws BadSyntax {
        if (condition) {
            throw format(String.format(format, parameters));
        }
    }

    /**
     * This method throws a {@code BadSyntax} exception with the format and the parameters.
     *
     * @param format    is a message format
     * @param parameters the parameter for the message format
     * @return does NOT return. It is declared retirning the exception, so you can throw the result of the method to aid the Java compiler control flow analysis.
     * @throws BadSyntax always
     */
    public static BadSyntax format(final String format, final Object... parameters) throws BadSyntax {
        throw castrated(new BadSyntax(String.format(format, parameters)));
    }

    /**
     * This method throws a {@code BadSyntax} exception with the format and the parameters. The provided exceptions is
     * the cause of the exception.
     *
     * @param e the cause of the exception
     * @param format is a message format
     * @param parameters the parameter for the message format
     * @return does NOT return. It is declared retirning the exception, so you can throw the result of the method to aid the Java compiler control flow analysis.
     * @throws BadSyntax always
     */
    public static BadSyntax format(final Exception e, final String format, final Object... parameters) throws BadSyntax {
        throw castrated(new BadSyntax(String.format(format, parameters), e));
    }


    /**
     * Remove the elements from the stack trace that are in this class. This all belongs to the exception throwing
     * and not relevant for the error itself.
     *
     * @param e the exception to remove the few top stack trace elements
     * @return the exception with the modified stack trace
     */
    private static BadSyntax castrated(final BadSyntax e) {
        final var st = Arrays.stream(e.getStackTrace()).filter(s -> !s.getClassName().equals(BadSyntax.class.getName())).toArray(StackTraceElement[]::new);
        e.setStackTrace(st);
        return e;
    }
}
