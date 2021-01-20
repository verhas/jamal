package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import static javax0.jamal.api.SpecialCharacters.QUERY;
import static javax0.jamal.api.SpecialCharacters.REPORT_ERRMES;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Try implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        final boolean query = firstCharIs(in, QUERY);
        final boolean report;
        if (query) {
            skip(in, 1);
            report = false;
        } else {
            report = firstCharIs(in, REPORT_ERRMES);
        }
        skipWhiteSpaces(in);
        try {
            final var result = processor.process(in);
            if (query) {
                return "true";
            } else {
                return result;
            }
        } catch (BadSyntax bs) {
            if (query) {
                return "false";
            }
            if (report) {
                return bs.getShortMessage();
            } else {
                return "";
            }
        }
    }
}