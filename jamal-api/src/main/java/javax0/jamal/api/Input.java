package javax0.jamal.api;

/**
 * An input has a string builder and also a file name from where the input is coming from. The
 * string builder is used to fetch the characters. The reference file name is used to construct the
 * name of the files in case a macro like {@code import} or {@code include} needs to open another file.
 */
public interface Input {
    /**
     * Get the {@link StringBuilder} that contains the characters of the input. The processing many times works directly
     * on the {@link StringBuilder} deleting characters from the start of it as the processing progresses, thus
     * essentially modifying/mutating the {@code Input} object.
     * @return the {@link StringBuilder} containing the characters of the input.
     */
    StringBuilder getInput();

    /**
     * Set the {@link StringBuilder} that will be held by this input.
     * @param input the new {@link StringBuilder} for this input.
     */
    void setInput(StringBuilder input);

    /**
     * @return the reference file name that the input was read from
     */
    String getReference();

    /**
     * Set the reference file name when the file is read.
     *
     * @param reference is to be set
     */
    void setReference(String reference);
}
