package javax0.jamal.api;

/**
 *
 * The input Jamal is working with.
 * This is essentially a stream of characters along with the position reference.
 * When a macro is processed many times the output is also the input of the surrounding macro.
 * Therefore, many times the class implementing this interface is also used as output.
 * The naming, therefore is a bit confusing.
 *
 * An input has a string builder and also a file name from where the input is coming from.
 * The string builder is used to fetch the characters.
 * The reference file name is used to construct the name of the files in case a macro like {@code import} or
 * {@code include} needs to open another file.
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
     * @return the line reference that contains the line number and also the filename.
     * There are methods to get the file name, line number and column position directly as a convenience.
     * The position also contains optionally a reference to a parent position.
     * That chain is used to maintain the "include list" hierarchy in error messages.
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
     * This operation also takes care maintaining the line number and column number position values.
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

    default boolean isEmpty() {
        return length() == 0;
    }

    /**
     * Find the string {@code s} in the input, similar as {@link #indexOf(String)}, but if the string is not found
     * before the index {@code before} then do not bother. The implementation may return {@code -1} or the position
     * larger than {@code before} where the string is found. The implementation does not need to implement a consistent
     * strategy. It may return -1 at one time and the real position larger than {@code before} at another time.
     *
     * @param s      the string to be found
     * @param before is a hint. If {@code s} can be found before {@code before} then it is okay. If it
     *               cannot be found then before the position the return value may be -1. In
     * @return the position of the string {@code s} in the input or -1 if it is not found. The return value may be -1
     * in case the string contains the string {@code s} but it is not found before {@code before} and {@code before}
     * is not -1.
     */
    default int indexOf(String s, int before) {
        return indexOf(s);
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
