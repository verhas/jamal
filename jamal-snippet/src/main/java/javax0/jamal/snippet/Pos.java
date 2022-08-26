package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

public class Pos implements Macro {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var what = in.toString().trim();
        switch (what) {
            case ".file":
                return in.getPosition().file;
            case ".line":
                return "" + in.getPosition().line;
            case ".column":
                return "" + in.getPosition().column;
            default:
                throw new BadSyntax(String.format("There is no '%s' part of the position, it can only be '.file', '.line' or '.column'", what));
        }
    }
}
