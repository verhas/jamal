package javax0.jamal.test.examples;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;

// snippet Array
public class Array implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var pos = in.getPosition();
        final String[] parts = InputHandler.getParts(in);
        BadSyntaxAt.when(parts.length < 2, "Macro Array needs an index and at least one element", pos);
        final int size = parts.length - 1;
        final int index;
        try {
            index = Integer.parseInt(parts[0]);
        } catch (NumberFormatException nfe) {
            throw new BadSyntaxAt("The index in Macro array '"
                    + parts[0]
                    + "' cannot be interpreted as an integer.", pos, nfe);
        }
        BadSyntaxAt.when(index < 0 || index >= parts.length - 1, "The index in Macro array is '"
                + parts[0]
                + "' but it should be between "
                + (-size) + " and " + (size - 1) + ".", pos);
        return parts[index + 1];
    }
}
// end snippet