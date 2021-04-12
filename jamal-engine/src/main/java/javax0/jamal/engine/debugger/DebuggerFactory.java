package javax0.jamal.engine.debugger;

import javax0.jamal.api.Debugger;
import javax0.jamal.engine.DebuggerStub;
import javax0.jamal.engine.NullDebugger;
import javax0.jamal.engine.Processor;

import java.util.Optional;

public class DebuggerFactory {

    public static Debugger build(Processor processor) {
        final var s = Optional.ofNullable(System.getProperty(Debugger.JAMAL_DEBUG_SYS)).orElseGet(
            () -> System.getenv(Debugger.JAMAL_DEBUG_ENV));
        if (s == null || s.length() == 0) {
            return new NullDebugger();
        }
        int min = Integer.MAX_VALUE;
        boolean unique = true;
        Debugger selected = null;
        for (final var debugger : Debugger.getInstances()) {
            final var affinity = debugger.affinity(s);
            if (affinity >= 0 && min > affinity) {
                unique = true;
                selected = debugger;
                min = affinity;
            } else if (min == affinity) {
                unique = false;
            }
        }
        if (!unique) {
            throw new IllegalArgumentException("There are two or more equal minimum affinity debuggers.");
        }

        try {
            selected.init(new DebuggerStub(processor));
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new IllegalArgumentException("There was an exception initializing the debugger.", e);
        }
        return selected;
    }
}
