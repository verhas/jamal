package javax0.jamal.api;

/**
 * This interface is not implemented by any class. The classes which are closers should implement the
 * {@link AutoCloseable} interface. They, however, may need to implement the {@link Closer.ProcessorAware} or
 * {@link Closer.OutputAware} interfaces (or both). This interface contains those interfaces.
 */
public interface Closer {

    /**
     * A macro, which is processor aware in the close operation should implement this interface, and then Jamal
     * will inject the {@link Processor} instance before invoking close.
     */
    interface ProcessorAware {
        /**
         * Implement this method to handle the processor injection into the macro before the closing.
         * Note that the processor is immutable and should be used to access macros and execution via the API.
         * @param processor is the processor instance used to execute this macro
         */
        void set(Processor processor);
    }

    /**
     * A macro, which is input aware in the close operation (needs the input) should implement this interface, and then
     * Jamal will inject the {@link Input} instance before invoking close.
     */
    interface OutputAware {
        /**
         * Implement this method to handle the input injection into the macro before the closing.
         * This object is not the Jamal input, but rather the output in an {@link Input} object.
         * This method is invoked directly before invoking {@link AutoCloseable#close()}.
         * The output ({@link Input}) object is mutable and the implemented {@link AutoCloseable#close()} can and is
         * allowed to modify the output object.
         *
         * @param output the output object.
         */
        void set(Input output);
    }
}
