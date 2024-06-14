package javax0.jamal.tools;

import javax0.jamal.api.MacroRegister;
import javax0.jamal.api.Marker;
import javax0.jamal.api.Position;

import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of the {@link javax0.jamal.api.Marker} interface that requires that the name passed as argument is the
 * same in the objects passed to {@link MacroRegister#pop(javax0.jamal.api.Marker) pop(Marker)} and to {@link
 * MacroRegister#push(javax0.jamal.api.Marker) push(Marker)}. The objects do not need to be the same, only the string
 * passed in {@code name} to the constructor has to be the same.
 */
public class NamedMarker implements Marker {
    final Position position;
    final String name;
    final Function<String, String> decorator;

    /**
     * @param name      is the identifier of the marker used to check the equality
     * @param decorator a decorator used to create the string representation of the marker. This string representation
     *                  is used in the error messages in the exceptions that are thrown.
     * @param position  is used in error messages and should point to the input position where the marker was created
     */
    public NamedMarker(String name, Function<String, String> decorator, Position position) {
        this.name = name;
        this.decorator = decorator;
        this.position = position;
    }

    /**
     * Convert the marker to string supporting the representation of the marker in the error messages.
     * The conversion uses the decorator function passed to the constructor.
     */
    @Override
    public String toString() {
        return decorator.apply(name);
    }

    /**
     * This implementation of {@code equals} checks that the name of the markers is the same.
     *
     * @param o the object to compare to
     * @return {@code true} if the object is a {@code NamedMarker} and the name of the two markers is the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedMarker that = (NamedMarker) o;
        return Objects.equals(name, that.name);
    }

    /**
     * This implementation of {@code hashCode} uses the name of the marker to calculate the hash code.
     *
     * @return the hash code of the name of the marker
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Get the position where the marker was created.
     *
     * @return the position
     */
    @Override
    public Position getPosition() {
        return position;
    }
}
