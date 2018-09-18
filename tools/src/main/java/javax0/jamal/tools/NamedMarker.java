package javax0.jamal.tools;

import javax0.jamal.api.Marker;

import java.util.Objects;
import java.util.function.Function;

public class NamedMarker implements Marker {

    final String name;
    final Function<String, String> decorator;

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
