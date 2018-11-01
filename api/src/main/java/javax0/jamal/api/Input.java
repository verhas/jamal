package javax0.jamal.api;

/**
 * An input has a string builder and also a file name from where the input is coming from. The
 * string builder is used to fetch the characters. The reference file name is used to construct the
 * name of the files in case a macro like {@code import} or {@code include} needs to open another file.
 */
public interface Input {
    StringBuilder getInput();

    void setInput(StringBuilder input);

    /**
     * @return the reference file name that the input was read from
     */
    String getReference();

    /**
     * Set the reference file name when the file is read.
     *
     * @param reference
     */
    void setReference(String reference);
}
