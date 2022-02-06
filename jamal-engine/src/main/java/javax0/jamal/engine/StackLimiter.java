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

    private static int getLimit() {
        final String limitString = EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_STACK_LIMIT_ENV).orElse(DEFAULT_LIMIT);
        try {
            return Integer.parseInt(limitString);
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(new BadSyntax("The environment variable " + EnvironmentVariables.JAMAL_STACK_LIMIT_ENV + " should be an integer"));
        }
    }

    private final AtomicInteger counter = new AtomicInteger(0);


    public void up() throws BadSyntax {
        if (counter.addAndGet(1) > LIMIT) {
            throw new BadSyntax("Jamal source seems to have infinite recursion");
        }
    }

    public int get() {
        return counter.get();
    }

    public int down() {
        final int downValue = counter.addAndGet(-1);
        if (downValue < 0) {
            throw new RuntimeException("Jamal has an internal error. Stack limiter went negative.");
        }
        return downValue;
    }

}
