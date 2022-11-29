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
     * {@code BadSyntaxAt} exception using the provided reference.
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

    public static String posFormat(Position pos) {
        if (pos == null) {
            return "";
        } else {
            return pos.file + "/" + pos.line + ":" + pos.column + (pos.parent == null ? "" : (" <<< " + posFormat(pos.parent)));
        }
    }

    @Override
    public String getMessage() {
        if (pos == null) {
            return super.getMessage();
        } else {
            return super.getMessage() + " at " + posFormat(pos);
        }
    }

    public interface Runnable {
        void run() throws BadSyntax;
    }

    public static class ThrowMayBe {
        private final BadSyntax e;

        public ThrowMayBe(BadSyntax e) {
            this.e = e;
        }

        public void orThrowWith(Position pos) throws BadSyntax {
            if (e != null) {
                throw new BadSyntaxAt(e, pos);
            }
        }
    }

    public static void when(final boolean condition, final BadSyntax.ThrowingSupplier<String> messeger, final Position pos) throws BadSyntaxAt {
        if (condition) {
            final String message;
            try {
                message = messeger.get();
            } catch (BadSyntax e) {
                throw new BadSyntaxAt(e, pos); // message.get() may throw, but never will
            }
            throw new BadSyntaxAt(message, pos);
        }
    }

    public static void when(final boolean condition, final String message, final Position pos) throws BadSyntaxAt {
        BadSyntaxAt.when(condition, () -> message, pos);
    }

    /**
     * Run a "runnable" that may throw a BadSyntax exception. In case it throws a {@link BadSyntax} exception but not a
     * {@link BadSyntaxAt}, which has the position information then return a {@link ThrowMayBe} instance that can be
     * used to enhance the exception with the position information.
     *
     * @param r the runnable
     * @return an object on which the {@link ThrowMayBe#orThrowWith(Position) orThrowWith()}  can be invoked.
     * @throws BadSyntax in case there was an exception thrown by the execution of the {@link Runnable}.
     */
    public static ThrowMayBe run(Runnable r) throws BadSyntax {
        try {
            r.run();
        } catch (BadSyntaxAt e) {
            throw e;
        } catch (BadSyntax e) {
            return new ThrowMayBe(e);
        }
        return new ThrowMayBe(null);
    }
}
