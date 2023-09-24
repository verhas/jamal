package javax0.jamal.api;

/**
 * A class, like the user-defined macro, can implement this interface and provide serialization and deserialization.
 * This feature is used to save references into a file and read the references from a file.
 * It is needed when there is a reference in the document that wants to use something that is defined later in the file.
 *
 * @param <T> is the type of the class that it serializes
 */
public interface Serializing<T extends Serializing<T>> {
    String serialize();

    T deserialize(String serialized) throws BadSyntax;
}
