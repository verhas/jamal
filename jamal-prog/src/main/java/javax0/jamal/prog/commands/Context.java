package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

public class Context {
    private final Processor processor;
    private int stepLimit;

    public Context(final Processor processor) {
        this(processor, 100_000);
    }

    public Context(final Processor processor, final int stepLimit) {
        this.processor = processor;
        this.stepLimit = stepLimit;
    }

    public void step() throws BadSyntax {
        if (stepLimit <= 0) {
            throw new BadSyntax("Step limit reached");
        }
        stepLimit--;
    }

    public Processor getProcessor() {
        return processor;
    }

}
