package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.TransientException;

/**
 * This exception is thrown when the references are not idempotent.
 * References are macros read from and saved into a separate file at the start and end of the processing.
 * If the values saved are not the same as read, it means that the processing was not idempotent.
 * It is usually caused by the change of some macro.
 * After the value of the macro edited, the first processing will save the new value and the idempotency error will disappear.
 * This is why this exception also implements the {@link TransientException} interface.
 */
public class IdempotencyFailed extends BadSyntax implements TransientException {

    public IdempotencyFailed(final String message) {
        super(message);
    }

    /**
     * This method throws an {@code IdempotencyFailed} exception when the {@code condition} is {@code true}.
     * The message of the exception is created using the {@code format} and the {@code parameters}.
     * <p>
     * The message formatting happens only when the {@code condition} is {@code true}. The arguments are, however,
     * evaluated before the call.
     *
     * @param condition when this parameter is {@code true} the method throws {@code BadSyntax} exception.
     *                  when the parameter is {@code false} the method returns.
     * @param format   is message format
     * @param parameters the parameter for the message format
     * @throws BadSyntax when the {@code condition} is {@code true}.
     */
    public static void when(final boolean condition, final String format, final Object... parameters) throws BadSyntax {
        if (condition) {
            throw new IdempotencyFailed(String.format(format, parameters));
        }
    }
}
