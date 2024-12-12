package javax0.jamal.api;

import java.util.ArrayList;

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
    public final ArrayList<String> segment = new ArrayList<>();
    public int line;
    public int column;
    public int charpos = 0;
    public final Position parent;

    /**
     * This is the position that this position is a clone of. This is needed when segments are to be added to or
     * removed from a position. The method {@link Input#getPosition()} returns a clone of the position but in the case
     * when we want to add a segment we need the original position as we want to modify it.
     */
    public final Position cloneOf;

    public Position(String file, int line, int column, Position parent) {
        this.file = file;
        this.line = line;
        this.column = column;
        this.parent = parent;
        this.cloneOf = this;
    }


    public Position(Position clone) {
        cloneOf = clone;
        if (clone == null) {
            file = null;
            line = 1;
            column = 1;
            charpos = 0;
            parent = null;
        } else {
            file = clone.file;
            line = clone.line;
            column = clone.column;
            parent = clone.parent;
            charpos = clone.charpos;
            segment.addAll(clone.segment);
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

    public void pushSegment(String segment) {
        this.segment.add(segment);
    }

    public void popSegment() {
        this.segment.remove(this.segment.size() - 1);
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
        final var forked = new Position(file, line, column, this);
        forked.segment.addAll(segment);
        return forked;
    }

    /**
     * Get the top position in the hierarchy.
     *
     * @return The top position is the one in the file that is on the top
     * level of the processing.
     */
    public Position top() {
        var top = this;
        while (top.parent != null) {
            top = top.parent;
        }
        return top;
    }

    public String posFormat() {
        if (segment.isEmpty()) {
            return file + "/" + line + ":" + column;
        } else {
            return file + "[" + String.join(">", segment) + "]/" + line + ":" + column;
        }
    }

    @Override
    public String toString() {
        return file + ":" + line + ":" + column;
    }

}
