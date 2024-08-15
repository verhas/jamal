package javax0.jamal.api;

import java.io.IOException;

/**
 * Classes implementing this interface can read different resource files.
 * An interface is a file, which is loaded and read not fomr the file system.
 * A typical example is a Java resource file that is loaded from the classpath.
 * <p>
 * The implementing class should implement the methods that can help the caller
 * to identify the appropriate reader and read the content of the resource.
 * <p>
 * For example, Java resources are read by the {@code ResourceInput} implementation.
 * It will return {@code true} from the {@link #canRead(String)} method if the file name
 * starts with {@code res:}.
 * It does not check in that phase that the file name is a valid resource name.
 * <p>
 * When an implementation is consulted calling the {@link #canRead(String)} method and it returns
 * {@code true} then the implementation is expected to be able to read the file.
 * The implementation should also implement the {@link #read(String)} method that reads the content as a string.
 * <p>
 * The implementation may also implement the {@link #readBinary(String)} method that reads the content as a byte array.
 * <p>
 * These methods also may have a second parameter {@code noCache} that is {@code true} if the content should not be
 * cached. The default implementations of these methods ignore this parameter in this interface.
 */
public interface ResourceReader extends ServiceLoaded {

    /**
     * The class can implement this method if it needs the Jamal processor to handle the resources to be read.
     *
     * @param processor the processor that is used to process the Jamal input that was referencing the resource.
     */
    default void setProcessor(final Processor processor) {
    }

    /**
     * Return {@code true} if the file name is accepted by this reader.
     *
     * @param fileName the name of the file to be read
     * @return {@code true} if the file name is accepted by this reader, {@code false} otherwise.
     */
    boolean canRead(final String fileName);

    /**
     * Return the index of the character where the actual file name starts.
     * It is used to calculate the absolute file name, when a file from the same resource type references another
     * using relative file name.
     * <p>
     * For example the file {@code res:jamal.jim} will return 4.
     * <p>
     * The caller should ensure that the file is handled by the actual resource type reader.
     * Implementation of this method may not detect the wrong prefix.
     * For example the implementation for the prefix {@code res:} will return constant 4.
     *
     * @param fileName the name of the file
     * @return the character index where the file name starts.
     */
    int fileStart(final String fileName);

    /**
     * Read the content of the resource as a UTF-8 encoded character stream
     *
     * @param fileName the name of the resource with the prefix
     * @return the content of the resource as a string
     * @throws IOException if the resource cannot be read
     */
    String read(String fileName) throws IOException;

    /**
     * Read the content if the resource as a UTF-8 encoded character stream. The default implementation of this
     * method calls the {@link #read(String)} method. In the default implementation the {@code noCache} parameter
     * is ignored.
     *
     * @param fileName the name of the resource with the prefix.
     * @param noCache  if {@code true} then the content of the resource should not be cached.
     * @return the content of the resource as a string
     * @throws IOException if the resource cannot be read
     */
    default String read(String fileName, boolean noCache) throws IOException {
        return read(fileName);
    }

    /**
     * Read the content of the resource as a binary stream.
     * @param fileName the name of the resource with the prefix
     * @param nocache if {@code true} then the content of the resource should not be cached.
     *                The default implementation of this method calls the {@link #readBinary(String)} method.
     * @return the content of the resource as a byte array
     * @throws IOException when the resource cannot be read
     */
    default byte[] readBinary(String fileName, boolean nocache) throws IOException {
        return readBinary(fileName);
    }

    /**
     * Read the content of the resource as a binary stream.
     * The default implementation of this method throws an {@link IOException} stating that binary resource reading
     * is not supported by this resource reader.
     *
     * @param fileName the name of the resource with the prefix
     * @return the content of the resource as a byte array
     * @throws IOException when the resource cannot be read
     */
    default byte[] readBinary(String fileName) throws IOException {
        throw new IOException("Binary resource reading is not supported by this resource reader.");
    }
}
