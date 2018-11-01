package javax0.jamal.tools;

import javax0.jamal.api.MacroRegister;
import javax0.jamal.api.Marker;

import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of the {@link javax0.jamal.api.Marker} interface that requires that the name passed as argument
 * is the same in the objects passed to {@link MacroRegister#pop(javax0.jamal.api.Marker)} and to
 * {@link MacroRegister#push(javax0.jamal.api.Marker)}. The objects do not need to be the same, only the string passed
 * in {@code name} to the constructor has to be the same.
 */
public class NamedMarker implements Marker {

    final String name;
    final Function<String, String> decorator;

    /**
     *
     * @param name is the identifier of the marker used to check the equality
     * @param decorator a decorator used to create the string representation of the marker. This string representation
     *                  is used in the error messages in the exceptions that are thrown.
     */
    public NamedMarker(String name, Function<String, String> decorator) {
        this.name = name;
        this.decorator = decorator;
    }

    @Override
    public String toString() {
        return decorator.apply(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedMarker that = (NamedMarker) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
