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
            parent = null;
        }else {
            file = clone.file;
            line = clone.line;
            column = clone.column;
            parent = clone.parent;
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

    public Position clone() {
        return new Position(this);
    }
}
