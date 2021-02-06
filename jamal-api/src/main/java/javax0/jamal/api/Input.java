package javax0.jamal.api;

/**
 * An input has a string builder and also a file name from where the input is coming from. The string builder is used to
 * fetch the characters. The reference file name is used to construct the name of the files in case a macro like {@code
 * import} or {@code include} needs to open another file.
 */
public interface Input extends CharSequence {
    /**
     * Get the {@link StringBuilder} that contains the characters of the input. The processing many times works directly
     * on the {@link StringBuilder} deleting characters from the start of it as the processing progresses, thus
     * essentially modifying/mutating the {@code Input} object.
     *
     * @return the {@link StringBuilder} containing the characters of the input.
     */
    StringBuilder getSB();

    /**
     * @return the line reference that contains the line number and also the filename
     */
    Position getPosition();

    /**
     * @return the reference file name that the input was read from
     */
    String getReference();

    /**
     * @return the current line number of the file where the input currently is in
     */
    int getLine();

    /**
     * @return the current column of the the line where the input currently is in
     */
    int getColumn();

    /**
     * Step the line number by one and reset the column number to 1.
     */
    void stepLine();

    /**
     * Step the column number by one.
     */
    void stepColumn();

    /**
     * @return the length of the input in terms of characters
     */
    @Override
    default int length() {
        return getSB().length();
    }

    @Override
    default char charAt(int index) {
        return getSB().charAt(index);
    }

    /**
     * Delete {@code numberOfCharacters} characters from the input at the start.
     *
     * @param numberOfCharacters the number of characters to be deleted
     * @return {@code this}
     */
    default Input delete(int numberOfCharacters) {
        var sb = getSB();
        for (int i = 0; i < numberOfCharacters && i < sb.length(); i++) {
            if (sb.charAt(i) == '\n') {
                stepLine();
            } else {
                stepColumn();
            }
        }
        getSB().delete(0, numberOfCharacters);
        return this;
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return getSB().subSequence(start, end);
    }

    /**
     * Appends the string representation of the {@code Object} argument.
     * <p>
     * The default implementation simply appends the string representation of the object to the underlying StringBuilder
     * returned by the method {@link #getSB()}.
     *
     * @param obj an {@code Object}.
     * @return {@code this}
     */
    default Input append(Object obj) {
        getSB().append(obj);
        return this;
    }

    /**
     * Delete all character from the input. Invokes {@link StringBuilder#setLength(int) setLength(0)} on the underlying
     * StringBuilder.
     */
    default void reset() {
        getSB().setLength(0);
    }

    /**
     * Returns the index of the string in the input. Method delegates the call simply to {@link
     * StringBuilder#indexOf(String)}.
     *
     * @param str the string we are looking for
     * @return whetever is returned by the {@link StringBuilder#indexOf(String)} method
     */
    default int indexOf(String str) {
        return getSB().indexOf(str);
    }

    /**
     * Proxy call to the underlying {@link StringBuilder#substring(int, int)}
     *
     * @param start see {@link StringBuilder#substring(int, int)}
     * @param end   see {@link StringBuilder#substring(int, int)}
     * @return see {@link StringBuilder#substring(int, int)}
     */
    default String substring(int start, int end) {
        return getSB().substring(start, end);
    }

    /**
     * Proxy call to the underlying {@link StringBuilder#substring(int)}
     *
     * @param start see {@link StringBuilder#substring(int)}
     * @return see {@link StringBuilder#substring(int)}
     */
    default String substring(int start) {
        return getSB().substring(start);
    }

    default void deleteCharAt(int i) {
        getSB().deleteCharAt(i);
    }
}
