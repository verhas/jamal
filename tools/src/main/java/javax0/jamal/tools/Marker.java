package javax0.jamal.tools;


import javax0.jamal.api.MacroRegister;

/**
 * Implementation of the {@link javax0.jamal.api.Marker} interface that requires that exactly the same objects
 * is passed to {@link MacroRegister#pop(javax0.jamal.api.Marker)} and to
 * {@link MacroRegister#push(javax0.jamal.api.Marker)}.
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
