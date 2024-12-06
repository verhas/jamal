package javax0.jamal.engine.debugger;

import javax0.jamal.api.Debugger;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.MinimumAffinityDebuggerSelector;
import javax0.jamal.tools.ProxyDebugger;

/**
 * The debugger factory finds and instantiates a debugger for the given processor. To perform this task the code looks
 * at the system property, or the environment variable {@link EnvironmentVariables#JAMAL_DEBUG_ENV}.
 * (If sys property is defined then the environment variable is ignored. The name of the system property is the
 * lowercase value of the environment variable with dots in place of the {@code _} characters.)
 * <p>
 * The value of the sys property or env variable is the configuration string for the debugger.
 * <p>
 * The processing then loads all the debuggers that implement the {@link Debugger} interface via service loader and it
 * asks each debugger about their willingness (affinity) to handle the debugging for the given configuration string. The
 * method {@link Debugger#affinity(String) affinity()} of the debugger returns a number. The debugger chooses the
 * debugger that returns the smallest non-negative number.
 * <p>
 * The processing always finds a debugger, because this module ({@code jamal.engine} defines a null debugger that
 * returns {@code Integer.MAX_VALUE-1} as an affinity value. In case there are no other debuggers the factory will find
 * this one.
 */
public class DebuggerFactory {

    /**
     * Get the debugger instance that had the best (lowest) affinity for the system property or environment defined
     * debugger configuration string.
     * <p>
     * Throws {@link IllegalArgumentException} if ther are more than one debuggers returning the same minimal affinity
     * so there is no way to select from or when the debugger initialization throws exception.
     *
     * @param processor the current processor
     * @return the dbugger instance and never {@code null}.
     */
    public static Debugger build(Processor processor) {
        final var s = EnvironmentVariables.getenv(EnvironmentVariables.JAMAL_DEBUG_ENV).orElse("");
        if (s.isEmpty()) {
            return new ProxyDebugger();
        }
        Debugger selected = MinimumAffinityDebuggerSelector.select(Debugger.getInstances(), s);
        try {
            if (processor.getDebuggerStub().isPresent()) {
                selected.init(processor.getDebuggerStub().get());
            } else {
                throw new IllegalArgumentException("There is no debugger stub for this processor. Weird.");
            }
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new IllegalArgumentException("There was an exception initializing the debugger.", e);
        }
        return selected;
    }
}
