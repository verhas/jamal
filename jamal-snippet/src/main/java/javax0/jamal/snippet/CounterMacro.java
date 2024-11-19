package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;
import javax0.jamal.tools.param.StringParameter;

import java.util.Arrays;
import java.util.stream.IntStream;

import static javax0.jamal.tools.InputHandler.*;

@Macro.Name("counter:define")
public class CounterMacro implements Macro, InnerScopeDependent, Scanner.FirstLine {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var scanner = newScanner(input, processor);
        final var id = scanner.str("id", "name").required();
        final var format = scanner.str("format").optional();
        final var start = scanner.str("start").defaultValue("1");
        final var step = scanner.str("step").defaultValue("1");
        final var iiii = scanner.bool("IIII");
        final var hierarchical = scanner.bool(null, "hierarchical");
        scanner.done();
        skipWhiteSpaces(input);
        BadSyntaxAt.when(input.length() > 0, "There are extra characters after the counter definition", input.getPosition());

        if (hierarchical.is()) {
            final var fmt = format.isPresent() ? format.get() : "%d" + IntStream.range(2, 30).mapToObj(i -> "{" + i + ":.%d}").reduce("", String::concat) + "{1:$title}";
            defineHierarchicalCounter(processor, id, fmt, start, step, iiii);
        } else {
            final var fmt = format.isPresent() ? format.get() : "%d";
            defineSimpleCounter(processor, id, fmt, toInt(start), toInt(step), iiii);
        }
        return "";
    }

    private static void defineSimpleCounter(Processor processor, StringParameter id, String format, int start, int step, BooleanParameter iiii) throws BadSyntax {
        if (isGlobalMacro(id.get())) {
            final var counter = new Counter(convertGlobal(id.get()), start, step, format, iiii.is(), processor);
            processor.defineGlobal(counter);
        } else {
            final var counter = new Counter(id.get(), start, step, format, iiii.is(), processor);
            processor.define(counter);
            // it has to be exported because it is inner scope dependent
            processor.getRegister().export(counter.getId());
        }
    }

    private static int toInt(StringParameter start) throws BadSyntax {
        try {
            return Integer.parseInt(start.get());
        } catch (NumberFormatException nfe) {
            throw new BadSyntax(String.format("The start value '%s' of the counter is not an integer", start.get()), nfe);
        }
    }

    private static void defineHierarchicalCounter(Processor processor, StringParameter id, String format, StringParameter start, StringParameter step, BooleanParameter iiii) throws BadSyntax {
        if (InputHandler.isGlobalMacro(id.get())) {
            final var counter = new CounterHierarchical(
                    processor,
                    InputHandler.convertGlobal(id.get()),
                    format,
                    iiii.is(),
                    toIntA(start),
                    toIntA(step));
            processor.defineGlobal(counter);
        } else {
            final var counter = new CounterHierarchical(
                    processor,
                    id.get(),
                    format,
                    iiii.is(),
                    toIntA(start),
                    toIntA(step));
            processor.define(counter);
            // it has to be exported because it is inner scope dependent
            processor.getRegister().export(counter.getId());
        }
    }

    private static int[] toIntA(StringParameter start) throws BadSyntax {
        return Arrays.stream(start.get().split("\\.")).mapToInt(Integer::parseInt).toArray();
    }

}
/*template jm_counter
{template |counter|counter:define id=$ID$ $FORMAT$ $START$ $STEP$ $IIII$ $HIERARCHICAL$ |define a counter|
        {variable |ID|"..."}
        {variable |FORMAT|format=%02d"}
        {variable |START|start=1"}
        {variable |STEP|step=1"}
        {variable |IIII|"IIII"}
        {variable |HIERARCHICAL|"hierarchical"}
}
 */
