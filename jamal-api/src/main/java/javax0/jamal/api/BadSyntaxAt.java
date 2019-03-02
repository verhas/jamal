package javax0.jamal.api;

public class BadSyntaxAt extends BadSyntax {
    final private Position pos;

    public Position getPosition() {
        return pos;
    }

    public BadSyntaxAt() {
        pos = new Position(null, 0);
    }

    public BadSyntaxAt(BadSyntax bs, Position pos) {
        super(bs.getMessage(), bs.getCause());
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
