package javax0.jamal.api;

/**
 * When a code is starting and ending a scope, it has to pass a marker object to the call to {@link
 * MacroRegister#push(Marker)} and {@link MacroRegister#pop(Marker)}. The {@code Marker} object passed to {@link
 * MacroRegister#pop(Marker)} should be equal to the one that was passed to {@link MacroRegister#push(Marker)}. This
 * helps to prevent the macro code from closing a scope in a way that it was not supposed to. For example using the macro
 * {@code @end} without a previously matching {@code @begin} in a {@code #block} macro or in an included file. See how
 * the macros {@code javax0.jamal.builtins.Begin} and {@code javax0.jamal.builtins.End} use the implementation of this
 * interface provided in the module tools.
 */
public interface Marker {
    /**
     * @return the position where the input was when the marker was created to start a new scope.
     */
    Position getPosition();

    /**
     * Create a **new** marker instance that can be used when there is no need for name, or position.
     * <p>
     * Since this method creates a new instance, every time it is called it cannot be replaced by a lambda expression.
     *
     * @return the new marker instance.
     */
    static Marker nullMarker() {
        return new Marker() {
            @Override
            public Position getPosition() {
                return null;
            }
        };
    }
}
