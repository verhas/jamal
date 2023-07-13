package javax0.jamal.api;

/**
 * Something that is configurable should implement this interface. Currently, the {@link UserDefinedMacro}
 * implementation does only.
 */
public interface Configurable {
    void configure(String key, Object value);
}
