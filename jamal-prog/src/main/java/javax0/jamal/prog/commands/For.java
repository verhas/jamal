package javax0.jamal.prog.commands;

import javax0.jamal.api.BadSyntax;

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
    public String execute(final Context ctx) throws BadSyntax {
        ctx.step();
        final var sb = new StringBuilder();
        String loopValue = start.execute(ctx);
        String step = this.step.execute(ctx);
        String end = this.end.execute(ctx);
        while (isDone(loopValue, step, end)) {
            Assignment.let(ctx.getProcessor(), variable, loopValue);
            sb.append(block.execute(ctx));
            if (Operation.bothNumeric(loopValue, step)) {
                loopValue = new BigInteger(loopValue).add(new BigInteger(step)).toString();
            } else {
                loopValue = loopValue + step;
            }
        }
        return sb.toString();
    }
}
