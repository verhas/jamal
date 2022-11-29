package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class CounterMacro implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var format = Params.<String>holder("format").orElse("%d");
        final var id = Params.<String>holder("id");
        final var start = Params.<Integer>holder("start").orElseInt(1);
        final var step = Params.<Integer>holder("step").orElseInt(1);
        skipWhiteSpaces(input);
        Scan.using(processor).from(this).firstLine().keys(format, start, step, id).parse(input);
        skipWhiteSpaces(input);
        BadSyntaxAt.when(input.length() > 0, () -> "There are extra characters after the counter definition",input.getPosition());

        final Counter counter;
        if (isGlobalMacro(id.get())) {
            counter = new Counter(convertGlobal(id.get()), start.get(), step.get(), format.get(), processor);
            processor.defineGlobal(counter);
        } else {
            counter = new Counter(id.get(), start.get(), step.get(), format.get(), processor);
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
