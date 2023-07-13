package javax0.jamal.api;

public interface Serializing<T extends Serializing<T>> {
    String serialize();
    T deserialize(String serialized) throws BadSyntax;
}
