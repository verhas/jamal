package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;

import java.math.BigInteger;

public class For implements Command {
    private final Expression start;
    private final Expression step;
    private final Expression end;
    private final String variable;

    private final Block block;

    public For(final Expression start, final Expression step, final Expression end, final String variable, final Block block) {
        this.start = start;
        this.step = step;
        this.end = end;
        this.variable = variable;
        this.block = block;
    }


    private static final String[] NO_PARAMS = new String[0];

    private boolean isDone(final String loopValue, final String step, final String end) {
        if (Operation.bothNumeric(loopValue, step)) {
            if (new BigInteger(step).compareTo(BigInteger.ZERO) < 0)
                return new BigInteger(loopValue).compareTo(new BigInteger(end)) >= 0;
            else
                return new BigInteger(loopValue).compareTo(new BigInteger(end)) <= 0;
        } else {
            return loopValue.compareTo(end) <= 0;
        }
    }

    @Override
    public String execute(final Processor processor) throws BadSyntax {
        final var sb = new StringBuilder();
        String loopValue = start.execute(processor);
        String step = this.step.execute(processor);
        String end = this.end.execute(processor);
        while (isDone(loopValue, step, end)) {
            Assignment.let(processor, variable, loopValue);
            sb.append(block.execute(processor));
            if (Operation.bothNumeric(loopValue, step)) {
                loopValue = new BigInteger(loopValue).add(new BigInteger(step)).toString();
            } else {
                loopValue = loopValue + step;
            }
        }
        return sb.toString();
    }
}
