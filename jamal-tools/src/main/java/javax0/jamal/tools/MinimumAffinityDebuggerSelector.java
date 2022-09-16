package javax0.jamal.tools;

import javax0.jamal.api.Debugger;

import java.util.List;

public class MinimumAffinityDebuggerSelector {
    public static Debugger select(final List<Debugger> debuggers,final String selector){
        int min = Integer.MAX_VALUE;
        boolean unique = true;
        Debugger selected = null;
        for (final var debugger : debuggers) {
            final var affinity = debugger.affinity(selector);
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
        if (selected == null) {
            throw new IllegalArgumentException("There is no debugger that can handle the given configuration string.");
        }
        return selected;
    }
}
