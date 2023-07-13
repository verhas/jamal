package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class CounterMacro implements Macro, InnerScopeDependent, Scanner.FirstLine {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var scanner = newScanner(input, processor);
        final var format = scanner.str("format").defaultValue("%d");
        final var id = scanner.str("id");
        final var start = scanner.number("start").defaultValue(1);
        final var step = scanner.number("step").defaultValue(1);
        final var iiii = scanner.bool("IIII");
        scanner.done();
        skipWhiteSpaces(input);
        BadSyntaxAt.when(input.length() > 0, "There are extra characters after the counter definition", input.getPosition());

        final Counter counter;
        if (isGlobalMacro(id.get())) {
            counter = new Counter(convertGlobal(id.get()), start.get(), step.get(), format.get(), iiii.is(), processor);
            processor.defineGlobal(counter);
        } else {
            counter = new Counter(id.get(), start.get(), step.get(), format.get(), iiii.is(), processor);
            processor.define(counter);
            // it has to be exported because it is inner scope dependent
            processor.getRegister().export(counter.getId());
        }
        return "";
    }

    @Override
    public String getId() {
        return "counter:define";
    }
}
