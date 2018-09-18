package javax0.jamal.tools;

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
