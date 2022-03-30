package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Position;

public class SnipCheckFailed extends BadSyntaxAt {
    private final String hashCode;
    private final String provided;

    public SnipCheckFailed(final String fileName, final String hashCode, final String provided, final String message, final Position pos) {
        super("The " + fileName + " hash is '" + hashCode +
                "' does not contain '" + provided + "'." + (message == null ? "" : ("\n'" + message + "'")),pos);
        this.hashCode = hashCode;
        this.provided = provided;
    }

    public String sed() {
        return "s/" + provided + "/" + hashCode.substring(0,8) + "/g";
    }
}
