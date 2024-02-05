package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Processor;
import javax0.jamal.engine.UserDefinedMacro;
import javax0.jamal.tools.Scanner;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;

@Macro.Name({"variation", "variations", "var"})
public class Variation implements Macro, Scanner, OptionsControlled {

    private static final String SPACE = "\\s+|\n|\r|\t|\f";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet pasted_params
        final var id = scanner.str(null, "id").defaultValue("$vari");
        // the identifier of the segment.
        // If you use only one variation, the default value for this paropt is `$vari` and you can omit the parameter.
        final var ignoreCase = scanner.bool(null, "ignoreCase");
        // ignore the case of the characters during the comparison.
        final var ignoreSpace = scanner.bool(null, "ignoreSpace");
        // treat repeated spaces as one space during the comparison.
        // Also, treat new line and other white-space characters as ordinary spaces for the comparison.
        final var trim = scanner.bool(null, "trim");
        // remove the training and leading spaces from the input before the comparison
        final var start = scanner.str("variation$start", "start").defaultValue("<<");
        // the start string of the parts that will not be compared
        final var end = scanner.str("variation$end", "end").defaultValue(">>");
        // the end of the string parts that will not be compared
        // end snippet
        scanner.done();
        final String startStr, endStr;
        final var existing = processor.getRegister().getUserDefined(id.get()).filter(m -> m instanceof VariationMacro).map(m -> (VariationMacro) m);
        if (existing.isPresent()) {
            var oldString = existing.get().fullyCleanedContent;
            existing.get().evaluate();
            var newString = in.toString();
            startStr = start.isPresent() && "start".equals(start.name()) ? start.get() : existing.get().start;
            endStr = end.isPresent() && "end".equals(end.name()) ? end.get() : existing.get().end;
            newString = clean(newString,
                    startStr,
                    endStr,
                    true);
            if (ignoreCase.is()) {
                oldString = oldString.toLowerCase();
                newString = newString.toLowerCase();
            }
            if (ignoreSpace.is()) {
                oldString = oldString.replaceAll(SPACE, " ");
                newString = newString.replaceAll(SPACE, " ");
            }
            if (trim.is()) {
                oldString = oldString.trim();
                newString = newString.trim();
            }
            BadSyntax.when(!oldString.equals(newString), "The copy/paste text '%s' was already defined and the content is different.", id.get());
        } else {
            startStr = start.get();
            endStr = end.get();
            final var macro = new VariationMacro(processor, convertGlobal(id.get()), in.toString());
            macro.cleanedContent = clean(in.toString(), start.get(), end.get(), false);
            macro.fullyCleanedContent = clean(in.toString(), start.get(), end.get(), true);
            macro.start = start.get();
            macro.end = end.get();
            if (isGlobalMacro(id.get())) {
                processor.defineGlobal(macro);
            } else {
                processor.define(macro);
            }
        }
        return clean(in.toString(), startStr, endStr, false);
    }

    /**
     * Remove all the substrings that start with the 'start' and end with the 'end' strings.
     *
     * @param input     the string to clean
     * @param start     the start string
     * @param end       the end string
     * @param fullClean if {@code true} then the start and end strings and the enclosed string will be removed, otherwise only the
     *                  start and end strings are removed
     * @return the cleaned string, that does not contain the enclosed substrings
     */
    private String clean(final String input, final String start, final String end, final boolean fullClean) throws BadSyntax {
        var sb = new StringBuilder();
        var i = 0;
        while (i < input.length()) {
            var j = input.indexOf(start, i);
            var e = input.indexOf(end, i);
            BadSyntax.when(0 < e && (e < j || j < 0), "There is a superfluous '%s' in the input string\n" +
                    "%s\nat character position %d", end, input, e);
            if (j < 0) {
                sb.append(input.substring(i));
                break;
            }
            sb.append(input, i, j);
            i = j + start.length();
            j = input.indexOf(end, i);
            e = input.indexOf(start, i);
            BadSyntax.when(0 < e && (e < j || j < 0), "There is a superfluous '%s' in the input string\n" +
                    "%s\nat character position %d", start, input, e);
            BadSyntax.when(j < 0, "Protected part starting with '%s' is not closed with '%s' in\n" +
                    "%s", start, end, input);
            if (!fullClean) {
                sb.append(input, i, j);
            }
            i = j + end.length();
        }
        return sb.toString();
    }

    private static class VariationMacro extends UserDefinedMacro implements AutoCloseable {
        private int counter = 1;
        private String cleanedContent;
        private String fullyCleanedContent;

        private String start;
        private String end;

        @Override
        public String evaluate(String... parameters) {
            counter++;
            return cleanedContent;
        }

        @Override
        public int expectedNumberOfArguments() {
            return 0;
        }

        public VariationMacro(Processor processor, String id, String content) throws BadSyntax {
            super((javax0.jamal.engine.Processor) processor, id, content);
            processor.deferredClose(this);
        }

        @Override
        public void close() throws Exception {
            BadSyntax.when(counter < 2, "The pasted macro '%s' was used only once.", getId());
        }
    }
}
