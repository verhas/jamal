package javax0.jamal.api;

/**
 * General signature interface for the objects for embedding application to pass context information to the
 * macros.
 * <p>
 * Context information can be anything the embedding application and some macros may have in common. It is
 * also possible to pass data from the embedding application to the macro implementations via thread-local variables,
 * but it is better to use this context pointer.
 * <p>
 * The context by default is {@code null}, and in case the embedding application needs a non-null pointer, then it should
 * invoke the constructor of the {@link Processor} implementation that sets the context.
 */
public interface Context {
}
