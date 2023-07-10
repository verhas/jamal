package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

@Deprecated(since = "2.4.0", forRemoval = true)
public class Update implements Macro, InnerScopeDependent, Scanner.FirstLine {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        scanner.str("head").defaultValue("");
        scanner.str("tail").defaultValue("");
        scanner.pattern("start").defaultValue("");
        scanner.pattern("stop").defaultValue("");
        scanner.done();

        BadSyntax.when(in.getPosition().file == null,
                "Cannot invoke update from an environment that has no file name");

        return "";
    }

    @Override
    public String getId() {
        return "snip:update";
    }
}
