package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.OptionsStore;
import javax0.jamal.tools.Scanner;
@Macro.Name({"options", "option"})
public class Options implements Macro, Scanner.Core {

    private enum PushPop {
        push, pop, set
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var operation = scanner.enumeration(PushPop.class).defaultValue(PushPop.set);
        scanner.done();

        final var os = OptionsStore.getInstance(processor);
        switch (operation.get(PushPop.class)) {
            case push:
                os.pushOptions(in.toString().split("\\||\\s+", -1));
                break;
            case pop:
                os.popOptions(in.toString().split("\\||\\s+", -1));
                break;
            case set:
                os.addOptions(in.toString().split("\\||\\s+", -1));
                break;
        }
        return "";
    }
}
