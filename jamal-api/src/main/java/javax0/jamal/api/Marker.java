package javax0.jamal.api;

/**
 * When a code is starting and ending a scope it has to pass a marker object to the call to
 * {@link MacroRegister#pop(Marker)} and {@link MacroRegister#push(Marker)}. The {@code Marker} object passed to
 * {@link MacroRegister#pop(Marker)} should be equal to the one that was passed to {@link MacroRegister#push(Marker)}.
 * This helps preventing the macro code to close a scope in a way that it was not supposed to. For example
 * using the macro {@code @end} without a previously matching {@code @begin} in a {@code #block} macro
 * or in an included file. See how the macros {@code javax0.jamal.builtins.Begin} and
 * {@code javax0.jamal.builtins.End} use the implementation of this interface provided in the tools module.
 */
public interface Marker {
}
