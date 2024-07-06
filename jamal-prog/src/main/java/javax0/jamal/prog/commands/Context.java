package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

import java.math.RoundingMode;

public class Context {
    private final Processor processor;
    private int stepLimit;
    private final boolean floating;
    private final RoundingMode roundingMode;
    private final int scale;

    public Context(final Processor processor) {
        this(processor, 100_000, false, RoundingMode.HALF_UP, 2);
    }

    public Context(final Processor processor, final int stepLimit, final boolean floating, RoundingMode roundingMode, int scale) {
        this.processor = processor;
        this.stepLimit = stepLimit;
        this.floating = floating;
        this.roundingMode = roundingMode;
        this.scale = scale;
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

    public boolean isFloating() {
        return floating;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    public int getScale() {
        return scale;
    }
}
