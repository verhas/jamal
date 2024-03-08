package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;

public class Counter implements Identified, Evaluable {
    final String id;
    int value;
    int lastValue;
    final int step;
    final String format;
    final Processor processor;

    final boolean iiii;

    public Counter(final String id, final int start, final int step, final String format, final boolean iiii, final Processor processor) {
        this.id = id;
        this.step = step;
        this.value = start;
        this.lastValue = start;
        this.format = format;
        this.processor = processor;
        this.iiii = iiii;
    }

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        final String s;
        if (parameters.length > 0) {
            if (parameters[0].equals("last")) {
                s = new CounterFormatter(format, iiii, id).formatValue(lastValue);
            } else if (!parameters[0].isEmpty() && parameters[0].charAt(0) == '>') {
                s = getAndIncrease();
                final var id = parameters[0].substring(1).trim();
                final var macro = processor.newUserDefinedMacro(convertGlobal(id), s, true);
                if (isGlobalMacro(id)) {
                    processor.defineGlobal(macro);
                } else {
                    processor.define(macro);
                }
                processor.getRegister().export(id);
            } else {
                s = getAndIncrease();
            }
        } else {
            s = getAndIncrease();
        }
        return s;
    }

    private String getAndIncrease() throws BadSyntax {
        final var s = new CounterFormatter(format, iiii, id).formatValue(value);
        lastValue = value;
        value += step;
        return s;
    }

    @Override
    public int expectedNumberOfArguments() {
        return 0;
    }

    @Override
    public String getId() {
        return id;
    }
}
