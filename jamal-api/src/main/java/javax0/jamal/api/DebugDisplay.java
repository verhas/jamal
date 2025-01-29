package javax0.jamal.api;

/**
 * Implement this interface in an {@link Identified} object to present the value of the object as a string
 * in the debugger.
 * <p>
 * If this interface is not implemented, then the object will simply be presented by its class name.
 * <p>
 * The fact that a class implements this interface is only making effect if it does NOT implement the
 * {@link Debuggable.UserDefinedMacro}. If that interface is implemented, then the values provided there will be used
 * to construct the debug display.
 */
public interface DebugDisplay {
    String debugDisplay();
}
