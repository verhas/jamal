package javax0.jamal.jamal;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

@Macro.Stateful
public class Output implements Macro, Scanner {
    /**
     * Instantiating a processor also instantiates all macro classes via the ServiceLoader. Instantiating here the
     * processor would instantiate a new instance of all the macros including this one and that would mean an infinite
     * recursion via the service loader.
     */
    private Processor localProc = null;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in,processor);
        final var isolate = scanner.bool("isolatedOutput","isolate");
        scanner.done();
        InputHandler.skipWhiteSpaces2EOL(in);
        if (isolate.is()) {
            try (var isolatedProc = new javax0.jamal.engine.Processor("{", "}")) {
                return isolatedProc.process(new javax0.jamal.tools.Input(in.toString(), in.getPosition()));
            }
        } else {
            if (localProc == null) {
                localProc = new javax0.jamal.engine.Processor("{", "}");
            }
            return localProc.process(new javax0.jamal.tools.Input(in.toString(), in.getPosition()));
        }
    }
}