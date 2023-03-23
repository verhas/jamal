package javax0.jamal.api;

import java.io.IOException;

public interface ResourceReader extends ServiceLoaded {

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
     * @param noCache if {@code true} then the content of the resource should not be cached.
     * @return the content of the resource as a string
     * @throws IOException if the resource cannot be read
     */
    default String read(String fileName, boolean noCache) throws IOException{
        return read(fileName);
    }
}
