package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

/**
 * Locate a file and return the file name (relative) where the file was found.
 */
public class Locate implements Macro, OptionsControlled, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in,processor);
        // the maximum number of directories to go up
        final var levelUpTo = scanner.number(null,"maxLevels").defaultValue(0);
        final var fileName = scanner.str("name").optional();
        scanner.done();
        return null;
    }

    @Override
    public String getId() {
        return "file:locate";
    }
}
