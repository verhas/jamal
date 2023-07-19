package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Evaluable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;

import java.util.ArrayList;
import java.util.List;

public class CounterHierarchical implements Identified, Evaluable, Scanner.WholeInput {
    final String id;

    @Override
    public String getId() {
        return id;
    }

    final String format;
    final boolean iiii;
    final List<Integer> counters = new ArrayList<>();
    final List<Boolean> used = new ArrayList<>();
    final int[] starts;
    final int[] steps;
    final Processor processor;
    boolean frozen;
    String title = "";

    public CounterHierarchical(final Processor processor, final String id, final String format, final boolean iiii, final int[] starts, final int[] steps, List<Integer> counters, List<Boolean> used, String title) {
        this.processor = processor;
        this.id = id;
        this.format = format;
        this.iiii = iiii;
        this.starts = starts;
        this.steps = steps;
        this.counters.addAll(counters);
        this.used.addAll(used);
        this.title = title;
    }

    public CounterHierarchical(final Processor processor, final String id, final String format, final boolean iiii, final int[] starts, final int[] steps) {
        this(processor, id, format, iiii, starts, steps, List.of(0 == starts.length ? 1 : starts[0]), List.of(false), "");
    }


    private enum Command {
        display, open, close, reset, last
    }

    @Override
    public String evaluate(String... parameters) throws BadSyntax {
        final var scanner = newScanner(Input.makeInput(parameters.length > 0 ? parameters[0] : ""), processor);
        final var command = scanner.enumeration(Command.class).optional();
        final var format = scanner.str(getId()+"$format", "format").defaultValue(this.format);
        final var saveAs = scanner.str(null, "save", "saveAs").optional();
        final var title = scanner.str(null, "title").defaultValue("");
        scanner.done();

        Command cmd = command.get(Command.class);
        switch (cmd) {
            case open:
                assertNotFrozen(cmd);
                assertNoTitle(title, cmd);
                final int index = counters.size();
                used.add(false);
                counters.add(getStart(index));
                break;
            case close:
                assertNotFrozen(cmd);
                assertNoTitle(title, cmd);
                counters.remove(counters.size() - 1);
                used.remove(used.size() - 1);
                break;
            case reset:
                assertNotFrozen(cmd);
                assertNoTitle(title, cmd);
                counters.clear();
                used.clear();
                counters.add(0, getStart(0));
                used.add(0, false);
                break;
            case display:
                if (used.get(used.size() - 1) && !frozen) {
                    stepLastLevel();
                }
                if(title.isPresent()){
                    this.title = title.get();
                }
                used.set(used.size() - 1, true);
            case last:
                if (cmd == Command.last) {
                    assertNotFrozen(cmd);
                    assertNoTitle(title, cmd);
                }
                final var fmt = formatCounter(format.get(), this.title);
                if (saveAs.isPresent()) {
                    saveCounter(saveAs.get());
                }
                return fmt;
        }
        return "";
    }

    private void assertNoTitle(StringParameter title, Command cmd) throws BadSyntax {
        BadSyntax.when(title.isPresent(), "The command %s does not accept title.", cmd.name());
    }

    private void assertNotFrozen(Command cmd) throws BadSyntax {
        BadSyntax.when(frozen, "The counter '%s' is frozen and cannot be modified using %s.", id, cmd.name());
    }

    private void saveCounter(String id) throws BadSyntax {
        final var macro = new CounterHierarchical(processor, id, format, iiii, starts, steps, counters, used, title);
        macro.frozen = true;
        if (InputHandler.isGlobalMacro(id)) {
            processor.defineGlobal(macro);
        } else {
            processor.define(macro);
        }
        processor.getRegister().export(id);
    }

    private String formatCounter(String format, String title) throws BadSyntax {
        final var fmt = new StringBuilder(format);
        int start;
        while ((start = fmt.indexOf("{")) > -1) {
            final int end = fmt.indexOf("}", start);
            BadSyntax.when(end == -1, "Format string '%s' is not valid.", format);
            final var fmt0 = fmt.substring(start + 1, end);
            final var parts = fmt0.split(":", 2);
            BadSyntax.when(parts.length < 2, "Format string '%s' is not valid, it has to be 'N:format'.", format);
            final int level;
            try {
                level = Integer.parseInt(parts[0]);
            } catch (NumberFormatException nfe) {
                throw new BadSyntax(String.format("Format string '%s' is not valid, it has to be 'N:format' where N is an integer.", format));
            }
            if (level < 1) {
                throw new BadSyntax(String.format("Format string '%s' is not valid, it has to be 'N:format' where N is a positive.", format));
            }
            final var fmt1 = parts[1];
            if (level > counters.size()) {
                fmt.replace(start, end + 1, "");
            } else {
                final var counter = counters.get(level - 1);
                final var fmt2 = new CounterFormatter(fmt1, iiii, id).formatValue(counter, title);
                fmt.replace(start, end + 1, fmt2);
            }
        }
        return new CounterFormatter(fmt.toString(), iiii, id).formatValue(counters.get(0), title);
    }

    private void stepLastLevel() {
        counters.set(counters.size() - 1, counters.get(counters.size() - 1) + getStep(counters.size() - 1));
    }

    private int getStart(int level) {
        return level >= starts.length ? 1 : starts[level];
    }

    private int getStep(int level) {
        return level >= steps.length ? 1 : steps[level];
    }

    @Override
    public int expectedNumberOfArguments() {
        return 1;
    }

}
