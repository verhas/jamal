package javax0.jamal.tools;

import javax0.jamal.api.Processor;

/**
 * A simple fluent class to validate the semantics of parops.
 * <p>
 * The typical use case for this would be
 * <p>
 * <pre>
 * {@code
 * if( Parop.validator(processor)
 *       .when(condition1).then("exception text1", paramarray1)
 *       .when(condition2).then("exception text2", paramarray2)
 *       ...
 *       .when(conditionN).then("exception textN", paramarrayN)
 *       .anyFailed()){
 *   return "";
 * }
 * }
 * </pre>
 *
 * It will check the conditions until one {@code true} is found and add a new BadSyntax exception to the deferred
 * exception list. Because it does not throw the exception itself, there is the need for the 'if' statement to return
 * from the macro.
 *
 */
public class Parop {

    final Processor processor;

    public Parop(Processor processor) {
        this.processor = processor;
    }

    public static WhenOn validator(Processor processor) {
        return new Parop(processor).new WhenOn();
    }

    public interface When {
        Then when(boolean b);

        boolean anyFailed();
        default boolean hasFailed() {
            return anyFailed();
        }
    }

    public final class WhenOff implements When {
        public Then when(boolean b) {
            return new ThenOff();
        }

        public boolean anyFailed() {
            return true;
        }
    }

    public final class WhenOn implements When {
        public Then when(boolean b) {
            if (b) {
                return new ThenTrue();
            } else {
                return new ThenFalse();
            }
        }

        public boolean anyFailed() {
            return false;
        }
    }

    public interface Then {
        When then(String format, Object... objetcs);
    }

    public final class ThenTrue implements Then {
        public When then(String format, Object... objects) {
            processor.deferredThrow(format, objects);
            return new WhenOff();
        }
    }

    public final class ThenOff implements Then {
        public When then(String format, Object... objects) {
            return new WhenOff();
        }
    }

    public final class ThenFalse implements Then {
        public When then(String format, Object... objects) {
            return new WhenOn();
        }
    }

}
