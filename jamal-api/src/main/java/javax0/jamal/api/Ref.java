package javax0.jamal.api;

/**
 * Reference is a reference object to another something that has an identifier.
 * It is a general idea to have a reference, therefore it is in the Api package and
 * currently it is used in the Yaml module.
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
