package javax0.jamal.api;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * This exception, as the name suggest, is thrown when the processor or a macro finds something it cannot interpret.
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

    final private List<String> parameters = new ArrayList<>();

    public List<String> getParameters() {
        return parameters;
    }

    public BadSyntax parameter(String param) {
        parameters.add(param);
        return this;
    }

    public BadSyntax parameters(List<String> param) {
        parameters.addAll(param);
        return this;
    }

    private static String abbreviate(String longParam) {
        if (longParam.length() > 60) {
            return longParam.substring(0, 60) + "...";
        }
        return longParam;
    }

    public String getShortMessage() {
        return super.getMessage();
    }

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
            throw new BadSyntax(message.get());
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
            throw new BadSyntax(String.format(format, parameters));
        }
    }
}
