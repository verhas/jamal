package javax0.jamal.tools;


import javax0.jamal.api.MacroRegister;

/**
 * Implementation of the {@link javax0.jamal.api.Marker} interface that requires that exactly the same objects is passed
 * to {@link MacroRegister#pop(javax0.jamal.api.Marker)} and to {@link MacroRegister#push(javax0.jamal.api.Marker)}.<p>
 * <p>
 * An instance of this class is used to pass to the {@link MacroRegister#push(javax0.jamal.api.Marker) push(Marker)}
 * call and when we call {@link MacroRegister#pop(javax0.jamal.api.Marker) pop(Marker)} the register checks that the
 * same instance was passed. This is much stricter than comparing only the {@code name} field. This is the reason why
 * this class does not define {@link Object#equals(Object)} and {@link Object#hashCode()} methods.<p>
 * <p>
 * When object identity cannot be guaranteed then the {@link NamedMarker} implementation of the {@link
 * javax0.jamal.api.Marker} interface has to be used. An example is the macro pair {@code begin} and {@code end}. See
 * their documentation at {@code javax0.jamal.builtins.Begin} and {@code javax0.jamal.builtins.End}.<p>
 * <p>
 * The {@code name} field in this implementation serves documentation purposes only and is included into the message of
 * the exception that is thrown by the method {@link MacroRegister#pop(javax0.jamal.api.Marker) pop(Marker)} when the
 * markers do not match.
 */
public class Marker implements javax0.jamal.api.Marker {
    final String name;

    public Marker(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
