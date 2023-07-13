package javax0.jamal.api;

/**
 * Exceptions, which may disappear just running the processing again by themselves implement this interface.
 * For the integrating modules, it means that the processing should or could be repeated.
 * For example, the IIntelliJJ Asciidoctor plugin preprocessor checks for this interface and does not cache the result.
 * When this type of exception appears, it will process the input again when requested by the plugin, even if the source did not change.
 * For example, {@code  IdempotencyFailed} in the snippet module is a transient exception.
 *
 */
public interface TransientException {
}
