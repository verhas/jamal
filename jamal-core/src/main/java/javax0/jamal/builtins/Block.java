package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

/**
 * Block macro used to enclose some operations into a scope. Can be used similarly as {@code Begin} and {@code End},
 * but the scope is not named. The two things are equivalent, it is a readability issue which one to use.
 * <p>
 * The implementation, as easily can be seen is the same as the macro {@link Comment}.
 */
public class Block implements Macro, InnerScopeDependent, OptionsControlled.Core, Scanner.Core {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var flat = scanner.bool(null, "flat", "export");
        scanner.done();
        if (flat.is()) {
            processor.getRegister().export();
        }
        return "";
    }
}
