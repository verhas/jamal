package javax0.jamal.api;

/**
 * This exception, as the name suggest, is thrown when the processor or a macro finds something it cannot interpret. The
 * 'At' at the end of the name suggest that the exception contains a {@link Position} object that tells exactly which
 * source file is the culprit and which line and character position is the place where the macro processor lost it's
 * faith in you being able to craft syntactically correct macros.
 */
public class BadSyntaxAt extends BadSyntax {
    final private Position pos;

    public Position getPosition() {
        return pos;
    }

    public BadSyntaxAt() {
        pos = new Position(null, 0);
    }

    /**
     * Convert a {@link BadSyntax} exception thrown from a macro that has no idea where the actual source code is to a
     * {@code BadSyntaxAt} exception using the provided reference.<p>
     * <p>
     * The new exception will inherit the message, the cause, the suppressed throwables and the stack trace of the
     * original exception.<p>
     *
     * @param bs  the original exception that was caught and is to be transformed to this exception
     * @param pos the position object denoting where the syntax error was actually detected
     */
    public BadSyntaxAt(BadSyntax bs, Position pos) {
        super(bs.getMessage(), bs.getCause());
        for (final var sup : bs.getSuppressed()) {
            addSuppressed(sup);
        }
        setStackTrace(bs.getStackTrace());
        this.pos = pos;
    }

    public BadSyntaxAt(String message, Position pos) {
        super(message);
        this.pos = pos;
    }

    public BadSyntaxAt(String message, Position pos, Throwable cause) {
        super(message, cause);
        this.pos = pos;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " at " + pos.file + "/" + pos.line + ":" + pos.column;
    }
}
