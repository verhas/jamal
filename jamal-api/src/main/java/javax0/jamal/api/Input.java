package javax0.jamal.api;

/**
 * An input has a string builder and also a file name from where the input is coming from. The
 * string builder is used to fetch the characters. The reference file name is used to construct the
 * name of the files in case a macro like {@code import} or {@code include} needs to open another file.
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

    default int length() {
        return getSB().length();
    }

    default char charAt(int index) {
        return getSB().charAt(index);
    }

    default Input delete(int start, int end) {
        var sb = getSB();
        for (int i = start; i < end && i < sb.length(); i++) {
            if (sb.charAt(i) == '\n') {
                stepLine();
            }else{
                stepColumn();
            }
        }
        getSB().delete(start, end);
        return this;
    }

    default CharSequence subSequence(int start, int end) {
        return getSB().subSequence(start, end);
    }

    default Input append(Object obj) {
        getSB().append(obj);
        return this;
    }

    default void reset() {
        getSB().setLength(0);
    }

    default int indexOf(String str) {
        return getSB().indexOf(str);
    }

    default String substring(int start, int end) {
        return getSB().substring(start, end);
    }

    default String substring(int start) {
        return getSB().substring(start);
    }
}
