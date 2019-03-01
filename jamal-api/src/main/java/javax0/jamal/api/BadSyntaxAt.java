package javax0.jamal.api;

public class BadSyntaxAt extends Exception {
    final private LineReference ref;

    public BadSyntaxAt() {
        ref = new LineReference(null, 0);
    }

    public BadSyntaxAt(BadSyntax bs, LineReference ref) {
        super(bs.getMessage(), bs.getCause());
        this.ref = ref;
    }

    public BadSyntaxAt(String message, LineReference ref) {
        super(message);
        this.ref = ref;
    }

    public BadSyntaxAt(String message, LineReference ref, Throwable cause) {
        super(message, cause);
        this.ref = ref;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " at " + ref.fileName + ":" + ref.lineNumber;
    }
}
