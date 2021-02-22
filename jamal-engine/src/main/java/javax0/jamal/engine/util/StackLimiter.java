package javax0.jamal.engine.util;

import javax0.jamal.api.BadSyntax;

import java.util.concurrent.atomic.AtomicInteger;

public class StackLimiter {
    private static final int LIMIT = getLimit();
    private static final String JAMAL_STACK_LIMIT = "JAMAL_STACK_LIMIT";
    private static final int DEFAULT_LIMIT = 300;

    private static int getLimit() {
        final String limitString = System.getenv(JAMAL_STACK_LIMIT);
        if (limitString == null) {
            return DEFAULT_LIMIT;
        }
        try {
            return Integer.parseInt(limitString);
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(new BadSyntax("The environment variable " + JAMAL_STACK_LIMIT + " should be an integer"));
        }
    }

    private final AtomicInteger counter = new AtomicInteger(0);


    public void up() throws BadSyntax {
        if (counter.addAndGet(1) > LIMIT) {
            throw new BadSyntax("Jamal source seems to have infinite recursion");
        }
    }

    public int down() {
        final int downValue = counter.addAndGet(-1);
        if (downValue < 0) {
            throw new RuntimeException("Jamal has an internal error. Stack limiter went negative.");
        }
        return downValue;
    }

}
