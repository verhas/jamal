package javax0.jamal.engine.debugger;

import javax0.jamal.api.Debugger;
import javax0.jamal.engine.DebuggerStub;
import javax0.jamal.engine.NullDebugger;
import javax0.jamal.engine.Processor;

public class DebuggerFactory {

    public static Debugger build(Processor processor) {
        final var s = System.getenv(Debugger.JAMAL_DEBUG);
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
        } catch (Exception e) {
            throw new IllegalArgumentException("There was an exception intializing the debugger.", e);
        }
        return selected;
    }
}
