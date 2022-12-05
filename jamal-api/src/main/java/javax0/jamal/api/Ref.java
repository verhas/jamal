package javax0.jamal.api;

/**
 * Reference is a reference object to another something that has an identifier.
 * Reference is a general idea usable by different external packages therefore it is in the Api package.
 * Currently, the Yaml module uses this class.
 */
public class Ref {
    public final String id;

    public Ref() {
        id = null;
    }

    public Ref(String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}
