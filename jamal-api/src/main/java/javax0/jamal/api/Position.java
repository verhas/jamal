package javax0.jamal.api;

/**
 * The {@code Position} contains the name of a file, a line number and the column number. This serves as a parameter
 * when an error happens. The exception {@link BadSyntaxAt} gets an object of this type as a parameter, and later it is
 * used to compose the message of the exception when the exception is logged.
 * <p>
 * The file name part of the position is also used to calculate the absolute file name when a macro parameter
 * references a file using relative file name, e.g. the {@code include} macro.
 */
public class Position {
    public final String file;
    public int line;
    public int column;
    public int charpos = 0;
    public final Position parent;

    public Position(String file, int line, int column, Position parent) {
        this.file = file;
        this.line = line;
        this.column = column;
        this.parent = parent;
    }

    public Position(Position clone) {
        if( clone == null ){
            file = null;
            line = 1;
            column = 1;
            charpos = 0;
            parent = null;
        }else {
            file = clone.file;
            line = clone.line;
            column = clone.column;
            parent = clone.parent;
            charpos = clone.charpos;
        }
    }

    public Position(String file, int line, int column) {
        this(file, line, column, null);
    }

    public Position(String file, int line) {
        this(file, line, 1);
    }

    public Position(String file) {
        this(file, 1);
    }

    /**
     * Creates a new instance from this one.
     *
     * @return the new instance
     */
    @Override
    public Position clone() {
        return new Position(this);
    }

    /**
     * @return return the position, which is the same as the position of the input, but references the position of the
     * input as parent. This means that we will have two positions, with the same file/line:column information, but
     * one will be the parent of the other.
     * <p>
     * This method can be used when a macro is evaluated. In that case the current position may be the actual position
     * in the file. When the macro is evaluated recursively evaluation of the macro may be much longer that the actual
     * use. An error may be reported at the wrong position in this case. With the hierarchical position we can report
     * the presumably "wrong" position, which does not reflect the real line number and column number in the file. At
     * the same time the hierarchy will also contain the position of the character in the file where the macro started.
     */
    public Position fork() {
        return new Position(file, line, column,this);
    }

    /**
     * Get the top position in the hierarchy.
     *
     * @return The top position is the one in the file that is on the top
     * level of the processing.
     */
    public Position top(){
        var top = parent;
        while( top.parent != null ){
            top = top.parent;
        }
        return top;
    }
}
