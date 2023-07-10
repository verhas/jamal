package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

public class Replace implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in,processor);
        final var isRegex = scanner.bool("regex");
        scanner.done();
        InputHandler.skipWhiteSpaces(in);
        final var parts = InputHandler.getParts(in);
        BadSyntax.when(parts.length < 2, () -> String.format("Marco 'replace' needs at least two arguments, got only %d:\n%s\n----------",
                        parts.length, String.join("\n", parts)));
        String string = parts[0];
        for (int i = 1; i < parts.length; i += 2) {
            final var from = parts[i];
            final String to;
            if (i < parts.length - 1) {
                to = parts[i + 1];
            } else {
                to = "";
            }
            if (isRegex.get()) {
                try {
                    string = string.replaceAll(from, to);
                } catch (IllegalArgumentException e) {
                    throw new BadSyntax("There is a problem with the regular expression in macro 'replace' : "
                        + from + "\n" + to + "\n", e);
                }
            } else {
                string = string.replace(from, to);
            }
        }
        return string;
    }
}
