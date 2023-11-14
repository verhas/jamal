package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.EnvironmentVariables;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class to be used in recursive calls to limit the depth of the call stack.
 * <p>
 * The default limit is {@code DEFAULT_LIMIT} (300) and it can be configured using the environment variable
 * {@code JAMAL_STACK_LIMIT}.
 * <p>
 * The calling code should use {@link #up()} and {@link #down()} to increment and decrement the counter.
 * When the counter reaches the limit in call to {@link #up()} a {@link BadSyntax} exception is thrown.
 * <p>
 * If the counter goes below zero in a call to {@link #down()} a {@link RuntimeException} is thrown.
 * In the second case the exception is not {@link BadSyntax} but a {@link RuntimeException} because it is a coding error
 * if the counter goes below zero. The calls to {@link #up()} and {@link #down()} must pair.
 */
public class StackLimiter {
    private static final int LIMIT = getLimit();
    private static final String DEFAULT_LIMIT = "300";

    /**
     * Retrieves the stack limit for Jamal from an environment variable.
     * <p>
     * This method reads the value of the stack limit from the environment variable specified by {@code EnvironmentVariables.JAMAL_STACK_LIMIT_ENV}.
     * If the environment variable is not set, it defaults to {@code DEFAULT_LIMIT}.
     * The value of the environment variable is expected to be an integer.
     *
     * @return The stack limit as an integer value.
     * @throws RuntimeException If the environment variable is set but cannot be parsed as an integer.
     *                          This exception wraps a {@code BadSyntax} exception with a message indicating the expected integer format.
     *                          <p>
     *                          The method performs the following operations:
     *                          1. Retrieves the value of the environment variable {@code EnvironmentVariables.JAMAL_STACK_LIMIT_ENV}.
     *                          If the environment variable is not set, it uses {@code DEFAULT_LIMIT}.
     *                          2. Attempts to parse the retrieved value as an integer.
     *                          3. If the parsing fails due to a {@code NumberFormatException}, it throws a {@code RuntimeException}
     *                          that wraps a {@code BadSyntax} exception with an appropriate error message.
     */
    private static int getLimit() {
        final String limitString = EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_STACK_LIMIT_ENV).orElse(DEFAULT_LIMIT);
        try {
            return Integer.parseInt(limitString);
        } catch (NumberFormatException nfe) {
            throw BadSyntax.rt("The environment variable " + EnvironmentVariables.JAMAL_STACK_LIMIT_ENV + " should be an integer");
        }
    }

    private final AtomicInteger counter = new AtomicInteger(0);


    /**
     * Increment the counter and throw {@link BadSyntax} if the counter reaches the limit.
     * <p>
     * This method should be called before a recursive call that is to be limited.
     *
     * @throws BadSyntax if the counter reaches the limit.
     */
    public void up() throws BadSyntax {
        BadSyntax.when(counter.addAndGet(1) > LIMIT, "Jamal source seems to have infinite recursion");
    }

    /**
     * Get the current value of the counter.
     *
     * @return the current value of the counter.
     */
    public int get() {
        return counter.get();
    }

    /**
     * Decrement the counter and throw {@link RuntimeException} if the counter goes below zero.
     * Every call of this method should be paired with a call to {@link #up()}.
     * Therefore, the counter-value cannot go below zero.
     * If it does, it is a coding error.
     *
     * @return the current value of the counter after the decrement.
     */
    public int down() {
        final int downValue = counter.addAndGet(-1);
        if (downValue < 0) {
            throw new RuntimeException("Jamal has an internal error. Stack limiter went negative.");
        }
        return downValue;
    }

}
